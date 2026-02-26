package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.GenerateImageRequest;
import com.dramagenerator.backend.model.AsyncTask;
import com.dramagenerator.backend.model.Drama;
import com.dramagenerator.backend.model.Episode;
import com.dramagenerator.backend.model.Scene;
import com.dramagenerator.backend.repository.EpisodeRepository;
import com.dramagenerator.backend.repository.SceneRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SceneServiceImpl implements SceneService {

    private final SceneRepository sceneRepository;
    private final EpisodeRepository episodeRepository;
    private final com.dramagenerator.backend.repository.DramaRepository dramaRepository;
    private final TaskService taskService;
    private final AIService aiService;
    private final ImageGenerationService imageGenerationService;
    private final com.dramagenerator.backend.repository.ImageGenerationRepository imageGenerationRepository;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private SceneServiceImpl self;

    @Override
    public String extractBackgroundsFromScript(Long episodeId, String model, String style) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Episode not found"));

        if (episode.getScriptContent() == null || episode.getScriptContent().isEmpty()) {
            throw new RuntimeException("Script content is empty");
        }

        AsyncTask task = taskService.createTask("background_extraction", String.valueOf(episode.getDrama().getId()));

        if (self != null) {
            self.processBackgroundExtraction(task.getId(), episode.getId(), model, style);
        } else {
             // Fallback
             processBackgroundExtraction(task.getId(), episode.getId(), model, style);
        }

        return task.getId();
    }

    @Async
    public void processBackgroundExtraction(String taskId, Long episodeId, String model, String style) {
        try {
            taskService.updateTaskStatus(taskId, "processing", 0, "Extracting scenes from script...");

            Episode episode = episodeRepository.findById(episodeId).orElse(null);
            if (episode == null) {
                throw new RuntimeException("Episode not found: " + episodeId);
            }
            
            String script = episode.getScriptContent();
            
            Drama drama = null;
            if (episode.getDrama() != null) {
                Long dramaId = episode.getDrama().getId();
                if (dramaId != null) {
                    drama = dramaRepository.findById(dramaId).orElse(null);
                }
            }
            
            String dramaStyle = "realistic";
            if (drama != null) {
                dramaStyle = drama.getStyle();
            }
            String finalStyle = style != null && !style.isEmpty() ? style : dramaStyle;

            String systemPrompt = String.format(
                    "[Task] Extract all unique scene backgrounds from the script\n" +
                    "[Requirements]\n" +
                    "1. Identify all different scenes (location + time combinations) in the script\n" +
                    "2. Generate detailed **English** image generation prompts for each scene\n" +
                    "3. **Important**: Scene descriptions must be **pure backgrounds** without any characters, people, or actions\n" +
                    "4. Prompt requirements:\n" +
                    "   - Must use **English**, no Chinese characters\n" +
                    "   - Detailed description of scene, time, atmosphere, style\n" +
                    "   - Must explicitly specify \"no people, no characters, empty scene\"\n" +
                    "   - Must match the drama's genre and tone\n" +
                    "   - **Style Requirement**: %s\n" +
                    "[Output Format]\n" +
                    "**CRITICAL: Return ONLY a valid JSON array. Do NOT include any markdown code blocks, explanations, or other text. Start directly with [ and end with ].**\n" +
                    "Each element containing:\n" +
                    "- location: Location (e.g., \"luxurious office\")\n" +
                    "- time: Time period (e.g., \"afternoon\")\n" +
                    "- prompt: Detailed image generation prompt (English)",
                    finalStyle
            );

            String userPrompt = "Script Content:\n" + script;

            Map<String, Object> options = new HashMap<>();
            if (model != null && !model.isEmpty()) {
                options.put("model", model);
            } else {
                // Check if active text config has a model, if not, let service decide
                // Or just don't set it and let service pick default
            }

            String response = aiService.generateText(userPrompt, systemPrompt, options);

            // Clean response
            if (response.contains("```json")) {
                response = response.substring(response.indexOf("```json") + 7);
                if (response.contains("```")) {
                    response = response.substring(0, response.indexOf("```"));
                }
            } else if (response.contains("```")) {
                response = response.substring(response.indexOf("```") + 3);
                if (response.contains("```")) {
                    response = response.substring(0, response.indexOf("```"));
                }
            }
            response = response.trim();

            List<Map<String, Object>> extractedScenes = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});

            taskService.updateTaskStatus(taskId, "processing", 50, "Saving scenes...");

            List<Scene> savedScenes = self.saveExtractedScenes(episodeId, extractedScenes);

            Map<String, Object> result = new HashMap<>();
            result.put("count", savedScenes.size());
            result.put("scenes", savedScenes);

            taskService.updateTaskResult(taskId, result);

        } catch (Exception e) {
            log.error("Background extraction failed", e);
            taskService.updateTaskError(taskId, e.getMessage());
        }
    }

    @Transactional
    public List<Scene> saveExtractedScenes(Long episodeId, List<Map<String, Object>> extractedScenes) {
        Episode episode = episodeRepository.findById(episodeId).orElseThrow(() -> new RuntimeException("Episode not found"));
        Drama drama = episode.getDrama(); // Drama is fetched within transaction scope here, so it's safe

        List<Scene> existingScenes = sceneRepository.findByEpisodeId(episodeId);
        List<Scene> scenesToDelete = new ArrayList<>();
        List<Scene> scenesToKeep = new ArrayList<>();
        
        for (Scene scene : existingScenes) {
            // Check if scene has image_url OR if it is referenced by image_generations
            // Since we can't easily check references without repository call or catch exception,
            // let's just catch the exception during delete or check if image_url is present.
            // But the error says FK constraint fails on image_generations.
            // This means there are ImageGeneration records pointing to this scene.
            // We should probably keep scenes that have related image generations, OR cascade delete them.
            // For now, let's just keep them if they have image_url (which implies successful generation).
            // But if generation failed or is pending, image_url might be null but ImageGeneration record exists.
            
            // To be safe and simple: let's try to delete and if it fails, we keep it.
            // Or better: we only delete scenes that we know are safe to delete.
            
            // If we want to clean up, we should probably delete related ImageGenerations first if we really want to remove the scene.
            // But here the logic seems to be: "remove old extracted scenes that don't have images yet, and replace with new extraction".
            
            if (scene.getImageUrl() != null && !scene.getImageUrl().isEmpty()) {
                scenesToKeep.add(scene);
            } else {
                scenesToDelete.add(scene);
            }
        }
        
        // Delete individually to handle constraints
        for (Scene scene : scenesToDelete) {
            try {
                // First try to delete related image generations if they don't have URL (failed/pending)
                // Actually, if we are deleting the scene, we probably want to delete its generations regardless?
                // But let's only delete if they are not valuable (no image url).
                // If they HAVE image url, we should have kept the scene in scenesToKeep!
                // So scenesToDelete ONLY contains scenes without image_url.
                // Thus, their generations are also likely failed or pending.
                // So it is safe to delete them.
                
                List<com.dramagenerator.backend.model.ImageGeneration> gens = imageGenerationRepository.findBySceneId(scene.getId());
                if (!gens.isEmpty()) {
                    imageGenerationRepository.deleteAll(gens);
                }
                
                sceneRepository.delete(scene);
            } catch (Exception e) {
                // If deletion still fails (e.g. other constraints), we just keep it
                // log.warn("Could not delete scene " + scene.getId() + ": " + e.getMessage());
                scenesToKeep.add(scene);
            }
        }

        List<Scene> savedScenes = new ArrayList<>(scenesToKeep);
        
        for (Map<String, Object> sceneData : extractedScenes) {
            String location = (String) sceneData.get("location");
            String time = (String) sceneData.get("time");
            String prompt = (String) sceneData.get("prompt");
            
            // Check if similar scene already exists in kept scenes
            boolean exists = false;
            for (Scene keptScene : scenesToKeep) {
                if (keptScene.getLocation() != null && keptScene.getLocation().equalsIgnoreCase(location) &&
                    keptScene.getTime() != null && keptScene.getTime().equalsIgnoreCase(time)) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                Scene scene = new Scene();
                scene.setDrama(drama);
                scene.setEpisode(episode);
                scene.setLocation(location);
                scene.setTime(time);
                scene.setPrompt(prompt);
                scene.setStatus("pending"); 
                
                scene = sceneRepository.save(scene);
                savedScenes.add(scene);
            }
        }
        return savedScenes;
    }

    @Override
    @Async
    public void batchGenerateScenes(Long episodeId) {
        List<Scene> scenes = sceneRepository.findByEpisodeId(episodeId);
        
        for (Scene scene : scenes) {
            if (self != null) {
                self.generateSceneImage(scene.getId(), "dall-e-3");
            } else {
                generateSceneImage(scene.getId(), "dall-e-3");
            }
        }
    }

    @Override
    @Async
    public void generateSceneImage(Long sceneId, String model) {
        try {
            Scene scene = sceneRepository.findById(sceneId).orElse(null);
            if (scene == null) return;
            
            scene.setStatus("generating");
            sceneRepository.save(scene);

            GenerateImageRequest req = new GenerateImageRequest();
            req.setDramaId(String.valueOf(scene.getDrama().getId()));
            req.setSceneId(scene.getId());
            req.setImageType("scene");
            req.setPrompt(scene.getPrompt());
            req.setModel(model); // Or config
            req.setSize("1024x1024");
            
            imageGenerationService.generateImage(req);
        } catch (Exception e) {
            log.error("Failed to generate scene image for scene " + sceneId, e);
        }
    }

    @Override
    public List<Scene> listScenes(Long episodeId) {
        return sceneRepository.findByEpisodeId(episodeId);
    }

    @Override
    public Scene getScene(Long id) {
        return sceneRepository.findById(id).orElseThrow(() -> new RuntimeException("Scene not found"));
    }

    @Override
    public Scene updateScene(Long id, Scene updates) {
        Scene scene = getScene(id);
        if (updates.getLocation() != null) scene.setLocation(updates.getLocation());
        if (updates.getTime() != null) scene.setTime(updates.getTime());
        if (updates.getPrompt() != null) scene.setPrompt(updates.getPrompt());
        if (updates.getImageUrl() != null) scene.setImageUrl(updates.getImageUrl());
        return sceneRepository.save(scene);
    }

    @Override
    public void deleteScene(Long id) {
        sceneRepository.deleteById(id);
    }
}
