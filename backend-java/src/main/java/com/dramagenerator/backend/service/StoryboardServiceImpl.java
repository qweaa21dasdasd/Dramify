package com.dramagenerator.backend.service;

import com.dramagenerator.backend.common.PromptI18n;
import com.dramagenerator.backend.dto.CreateStoryboardRequest;
import com.dramagenerator.backend.model.*;
import com.dramagenerator.backend.model.Character;
import com.dramagenerator.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryboardServiceImpl implements StoryboardService {

    private final StoryboardRepository storyboardRepository;
    private final EpisodeRepository episodeRepository;
    private final DramaRepository dramaRepository;
    private final CharacterRepository characterRepository;
    private final SceneRepository sceneRepository;
    private final ImageGenerationRepository imageGenerationRepository;
    private final TaskService taskService;
    private final AIService aiService;
    private final PromptI18n promptI18n;
    private final ObjectMapper objectMapper;

    @Override
    public String generateStoryboard(Long episodeId, String model) {
        // 1. Get Episode and Drama
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Episode not found"));
        
        // 2. Get Script Content
        String scriptContent = episode.getScriptContent();
        if (scriptContent == null || scriptContent.isEmpty()) {
            scriptContent = episode.getDescription();
        }
        if (scriptContent == null || scriptContent.isEmpty()) {
            throw new RuntimeException("Script content is empty");
        }

        // 3. Get Characters
        List<Character> characters = characterRepository.findByDramaId(episode.getDrama().getId());
        String characterList = formatCharacterList(characters);

        // 4. Get Scenes
        List<Scene> scenes = sceneRepository.findByDramaId(episode.getDrama().getId());
        String sceneList = formatSceneList(scenes);

        // 5. Build Prompt
        String prompt = buildPrompt(scriptContent, characterList, sceneList);

        // 6. Create Task
        AsyncTask task = taskService.createTask("storyboard_generation", String.valueOf(episodeId));

        // 7. Start Async Process
        processStoryboardGeneration(task.getId(), episodeId, model, prompt);

        return task.getId();
    }

    @Async
    public void processStoryboardGeneration(String taskId, Long episodeId, String model, String prompt) {
        try {
            taskService.updateTaskStatus(taskId, "processing", 10, "Starting generation...");

            // Call AI
            Map<String, Object> options = new HashMap<>();
            options.put("max_tokens", 16000);
            if (model != null && !model.isEmpty()) {
                options.put("model", model);
            }
            
            String response = aiService.generateText(prompt, promptI18n.getStoryboardSystemPrompt(), options);

            taskService.updateTaskStatus(taskId, "processing", 50, "Parsing result...");

            // Parse Result
            List<StoryboardDTO> storyboardDTOs = parseAIResponse(response);
            
            taskService.updateTaskStatus(taskId, "processing", 70, "Saving storyboards...");

            // Save Storyboards
            saveStoryboards(episodeId, storyboardDTOs);

            // Update Episode Duration
            int totalDuration = storyboardDTOs.stream().mapToInt(StoryboardDTO::getDuration).sum();
            int durationMinutes = (totalDuration + 59) / 60;
            
            Episode episode = episodeRepository.findById(episodeId).orElseThrow();
            episode.setDuration(durationMinutes);
            episodeRepository.save(episode);

            // Update Task Result
            Map<String, Object> result = new HashMap<>();
            result.put("storyboards", storyboardDTOs);
            result.put("total", storyboardDTOs.size());
            result.put("total_duration", totalDuration);
            
            taskService.updateTaskResult(taskId, result);

        } catch (Exception e) {
            log.error("Failed to generate storyboard", e);
            taskService.updateTaskError(taskId, e.getMessage());
        }
    }

    private String formatCharacterList(List<Character> characters) {
        if (characters.isEmpty()) return "No characters";
        return characters.stream()
                .map(c -> String.format("{\"id\": %d, \"name\": \"%s\"}", c.getId(), c.getName()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String formatSceneList(List<Scene> scenes) {
        if (scenes.isEmpty()) return "No scenes";
        return scenes.stream()
                .map(s -> String.format("{\"id\": %d, \"location\": \"%s\", \"time\": \"%s\"}", s.getId(), s.getLocation(), s.getTime()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String buildPrompt(String script, String characters, String scenes) {
        // Simplified prompt construction for now, ideally fetch from PromptI18n or resource file
        return String.format(
            "Script:\n%s\n\nCharacters:\n%s\n\nScenes:\n%s\n\n" +
            "Please break down the script into storyboards in JSON format. " +
            "Each storyboard should have: shot_number, title, shot_type, angle, time, location, scene_id (optional), " +
            "movement, action, dialogue, result, atmosphere, emotion, duration (seconds), bgm_prompt, sound_effect, characters (list of IDs).",
            script, characters, scenes
        );
    }

    @Data
    public static class StoryboardDTO {
        private Integer shot_number;
        private String title;
        private String shot_type;
        private String angle;
        private String time;
        private String location;
        private Long scene_id;
        private String movement;
        private String action;
        private String dialogue;
        private String result;
        private String atmosphere;
        private String emotion;
        private Integer duration;
        private String bgm_prompt;
        private String sound_effect;
        private List<Long> characters;
        
        public Integer getDuration() { return duration != null ? duration : 5; }
    }
    
    @Data
    public static class StoryboardResponse {
        private List<StoryboardDTO> storyboards;
    }

    private List<StoryboardDTO> parseAIResponse(String response) throws JsonProcessingException {
        // Try parsing as object with "storyboards" field
        try {
            StoryboardResponse res = objectMapper.readValue(response, StoryboardResponse.class);
            if (res.getStoryboards() != null) return res.getStoryboards();
        } catch (Exception e) {
            // ignore
        }
        
        // Try parsing as array
        try {
            return objectMapper.readValue(response, new TypeReference<List<StoryboardDTO>>() {});
        } catch (Exception e) {
            // Try to extract JSON from markdown block
            if (response.contains("```json")) {
                int start = response.indexOf("```json") + 7;
                int end = response.lastIndexOf("```");
                if (end > start) {
                    String json = response.substring(start, end).trim();
                    return parseAIResponse(json);
                }
            }
            throw new RuntimeException("Failed to parse AI response: " + response);
        }
    }

    @Transactional
    protected void saveStoryboards(Long episodeId, List<StoryboardDTO> dtos) {
        // Delete existing storyboards (and clear image gen associations if needed)
        // For simplicity, assuming cascade delete or manual cleanup isn't strictly required for MVP unless foreign keys restrict it
        // Ideally: imageGenerationRepository.updateStoryboardIdToNull(storyboardIds);
        storyboardRepository.deleteByEpisodeId(episodeId);

        Episode episode = episodeRepository.getReferenceById(episodeId);

        for (StoryboardDTO dto : dtos) {
            Storyboard sb = new Storyboard();
            sb.setEpisode(episode);
            sb.setStoryboardNumber(dto.getShot_number());
            sb.setTitle(dto.getTitle());
            sb.setLocation(dto.getLocation());
            sb.setTime(dto.getTime());
            sb.setShotType(dto.getShot_type());
            sb.setAngle(dto.getAngle());
            sb.setMovement(dto.getMovement());
            sb.setAction(dto.getAction());
            sb.setResult(dto.getResult());
            sb.setAtmosphere(dto.getAtmosphere());
            sb.setDialogue(dto.getDialogue());
            sb.setDuration(dto.getDuration());
            sb.setBgmPrompt(dto.getBgm_prompt());
            sb.setSoundEffect(dto.getSound_effect());
            
            if (dto.getScene_id() != null) {
                // Check if scene exists? Or just set ID if mapped
                sceneRepository.findById(dto.getScene_id()).ifPresent(sb::setScene);
            }

            // Generate Prompts
            sb.setImagePrompt(generateImagePrompt(dto));
            sb.setVideoPrompt(generateVideoPrompt(dto));

            sb = storyboardRepository.save(sb);

            // Associate Characters
            if (dto.getCharacters() != null && !dto.getCharacters().isEmpty()) {
                List<Character> chars = characterRepository.findAllById(dto.getCharacters());
                sb.setCharacters(chars);
                storyboardRepository.save(sb);
            }
        }
    }

    private String generateImagePrompt(StoryboardDTO dto) {
        // Implement prompt generation logic based on Go version
        List<String> parts = new ArrayList<>();
        if (dto.getLocation() != null) parts.add(dto.getLocation());
        if (dto.getTime() != null) parts.add(dto.getTime());
        if (dto.getAction() != null) parts.add(dto.getAction()); // Should extract initial pose
        if (dto.getAtmosphere() != null) parts.add(dto.getAtmosphere());
        parts.add("anime style, first frame");
        return String.join(", ", parts);
    }

    private String generateVideoPrompt(StoryboardDTO dto) {
        // Implement video prompt generation
        List<String> parts = new ArrayList<>();
        if (dto.getAction() != null) parts.add("Action: " + dto.getAction());
        if (dto.getMovement() != null) parts.add("Camera: " + dto.getMovement());
        if (dto.getLocation() != null) parts.add("Scene: " + dto.getLocation());
        return String.join(". ", parts);
    }

    @Override
    @Transactional
    public void updateStoryboard(Long id, Map<String, Object> updates) {
        Storyboard sb = storyboardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Storyboard not found"));
        
        // Apply updates
        if (updates.containsKey("title")) sb.setTitle((String) updates.get("title"));
        if (updates.containsKey("storyboard_number")) sb.setStoryboardNumber(((Number) updates.get("storyboard_number")).intValue());
        if (updates.containsKey("shot_type")) sb.setShotType((String) updates.get("shot_type"));
        if (updates.containsKey("angle")) sb.setAngle((String) updates.get("angle"));
        if (updates.containsKey("movement")) sb.setMovement((String) updates.get("movement"));
        if (updates.containsKey("action")) sb.setAction((String) updates.get("action"));
        if (updates.containsKey("dialogue")) sb.setDialogue((String) updates.get("dialogue"));
        if (updates.containsKey("description")) sb.setDescription((String) updates.get("description"));
        if (updates.containsKey("result")) sb.setResult((String) updates.get("result"));
        if (updates.containsKey("atmosphere")) sb.setAtmosphere((String) updates.get("atmosphere"));
        if (updates.containsKey("bgm_prompt")) sb.setBgmPrompt((String) updates.get("bgm_prompt"));
        if (updates.containsKey("sound_effect")) sb.setSoundEffect((String) updates.get("sound_effect"));
        if (updates.containsKey("duration")) sb.setDuration(((Number) updates.get("duration")).intValue());
        if (updates.containsKey("location")) sb.setLocation((String) updates.get("location"));
        if (updates.containsKey("time")) sb.setTime((String) updates.get("time"));
        if (updates.containsKey("scene_id")) {
             Number sceneId = (Number) updates.get("scene_id");
             if (sceneId != null) {
                 sceneRepository.findById(sceneId.longValue()).ifPresent(sb::setScene);
             } else {
                 sb.setScene(null);
             }
        }
        
        // Re-generate prompts if key fields change
        // This is a simplified logic. In Go version it might be more complex.
        // For now, let's update prompts if action/location/etc changes.
        // Actually, let's keep it simple: if prompts are provided in updates, use them.
        // If not, maybe regenerate? Let's assume frontend sends updates prompts or we don't auto-update them on partial edits to avoid overwriting user edits.
        
        if (updates.containsKey("image_prompt")) sb.setImagePrompt((String) updates.get("image_prompt"));
        if (updates.containsKey("video_prompt")) sb.setVideoPrompt((String) updates.get("video_prompt"));
        
        storyboardRepository.save(sb);
    }

    @Override
    @Transactional
    public Storyboard createStoryboard(CreateStoryboardRequest req) {
        Episode episode = episodeRepository.findById(req.getEpisodeId())
                .orElseThrow(() -> new RuntimeException("Episode not found"));
        
        Storyboard sb = new Storyboard();
        sb.setEpisode(episode);
        sb.setStoryboardNumber(req.getStoryboardNumber());
        sb.setTitle(req.getTitle());
        sb.setLocation(req.getLocation());
        sb.setTime(req.getTime());
        sb.setShotType(req.getShotType());
        sb.setAngle(req.getAngle());
        sb.setMovement(req.getMovement());
        sb.setDescription(req.getDescription());
        sb.setAction(req.getAction());
        sb.setResult(req.getResult());
        sb.setAtmosphere(req.getAtmosphere());
        sb.setDialogue(req.getDialogue());
        sb.setBgmPrompt(req.getBgmPrompt());
        sb.setSoundEffect(req.getSoundEffect());
        sb.setDuration(req.getDuration());
        
        if (req.getSceneId() != null) {
            sceneRepository.findById(req.getSceneId()).ifPresent(sb::setScene);
        }
        
        // Initial Prompts
        StoryboardDTO dto = new StoryboardDTO();
        dto.setLocation(req.getLocation());
        dto.setTime(req.getTime());
        dto.setAction(req.getAction());
        dto.setAtmosphere(req.getAtmosphere());
        dto.setMovement(req.getMovement());
        
        sb.setImagePrompt(generateImagePrompt(dto));
        sb.setVideoPrompt(generateVideoPrompt(dto));
        
        sb = storyboardRepository.save(sb);
        
        if (req.getCharacters() != null && !req.getCharacters().isEmpty()) {
            List<Character> chars = characterRepository.findAllById(req.getCharacters());
            sb.setCharacters(chars);
            storyboardRepository.save(sb);
        }
        
        return sb;
    }

    @Override
    @Transactional
    public void deleteStoryboard(Long id) {
        storyboardRepository.deleteById(id);
    }
}
