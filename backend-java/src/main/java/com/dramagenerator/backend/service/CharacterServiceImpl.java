package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.GenerateImageRequest;
import com.dramagenerator.backend.model.AsyncTask;
import com.dramagenerator.backend.model.Character;
import com.dramagenerator.backend.model.Drama;
import com.dramagenerator.backend.model.Episode;
import com.dramagenerator.backend.model.ImageGeneration;
import com.dramagenerator.backend.repository.CharacterRepository;
import com.dramagenerator.backend.repository.DramaRepository;
import com.dramagenerator.backend.repository.EpisodeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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

import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CharacterServiceImpl implements CharacterService {

    private final CharacterRepository characterRepository;
    private final EpisodeRepository episodeRepository;
    private final DramaRepository dramaRepository;
    private final TaskService taskService;
    private final AIService aiService;
    private final ImageGenerationService imageGenerationService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private CharacterService self;

    @Override
    public String extractCharacters(com.dramagenerator.backend.dto.GenerateCharactersRequest request) {
        if (request.getEpisodeId() != null) {
            return extractCharactersFromScript(request.getEpisodeId());
        }
        
        if (request.getDramaId() == null) {
            throw new RuntimeException("Drama ID is required");
        }
        
        Drama drama = dramaRepository.findById(request.getDramaId())
                .orElseThrow(() -> new RuntimeException("Drama not found"));
                
        String content = request.getOutline();
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("Script content or outline is required");
        }
        
        AsyncTask task = taskService.createTask("character_extraction", String.valueOf(drama.getId()));
        
        self.processCharacterExtractionGeneric(task.getId(), drama.getId(), content, null);
        
        return task.getId();
    }

    @Override
    public String extractCharactersFromScript(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Episode not found"));
        
        if (episode.getScriptContent() == null || episode.getScriptContent().isEmpty()) {
            throw new RuntimeException("Script content is empty");
        }
        
        AsyncTask task = taskService.createTask("character_extraction", String.valueOf(episode.getDrama().getId()));
        
        self.processCharacterExtractionGeneric(task.getId(), episode.getDrama().getId(), episode.getScriptContent(), episodeId);
        
        return task.getId();
    }

    @Async
    public void processCharacterExtraction(String taskId, Episode episode) {
        // Deprecated, delegating to generic
        self.processCharacterExtractionGeneric(taskId, episode.getDrama().getId(), episode.getScriptContent(), episode.getId());
    }

    @Async
    public void processCharacterExtractionGeneric(String taskId, Long dramaId, String content, Long episodeId) {
        try {
            taskService.updateTaskStatus(taskId, "processing", 0, "Analyzing content...");
            
            Drama drama = dramaRepository.findById(dramaId).orElse(null);
            String style = drama != null ? drama.getStyle() : "realistic";
            
            String systemPrompt = "You are a professional script analyst. Extract characters from the provided script/outline content. " +
                    "Return a JSON array of objects with fields: name, role (protagonist/antagonist/supporting), " +
                    "appearance (detailed physical description), personality, description. " +
                    "Focus on visual details for appearance. Style: " + style;
            
            String userPrompt = "Content:\n" + content;
            
            Map<String, Object> options = new HashMap<>();
            options.put("model", "gpt-4o"); // Or from config
            
            String response = aiService.generateText(userPrompt, systemPrompt, options);
            
            // Parse response (handle markdown code blocks)
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
            
            List<Map<String, Object>> extractedChars = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
            
            taskService.updateTaskStatus(taskId, "processing", 50, "Saving characters...");
            
            List<Character> savedChars = self.saveExtractedCharacters(dramaId, episodeId, extractedChars);
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", savedChars.size());
            result.put("characters", savedChars);
            
            taskService.updateTaskResult(taskId, result);
            
        } catch (Exception e) {
            log.error("Character extraction failed", e);
            taskService.updateTaskError(taskId, e.getMessage());
        }
    }

    @Transactional
    public List<Character> saveExtractedCharacters(Long dramaId, Long episodeId, List<Map<String, Object>> extractedChars) throws Exception {
        List<Character> savedChars = new ArrayList<>();
        Drama drama = dramaRepository.findById(dramaId).orElseThrow(() -> new RuntimeException("Drama not found"));
        Episode episode = null;
        if (episodeId != null) {
            episode = episodeRepository.findById(episodeId).orElse(null);
            // Initialize characters collection to avoid LazyInitializationException
            if (episode != null) {
                episode.getCharacters().size(); 
            }
        }

        for (Map<String, Object> charData : extractedChars) {
            String name = (String) charData.get("name");
            
            // Check duplicate
            Character character = characterRepository.findByDramaId(dramaId).stream()
                    .filter(c -> c.getName().equals(name))
                    .findFirst()
                    .orElse(null);
            
            if (character == null) {
                character = new Character();
                character.setDrama(drama);
                character.setName(name);
                character.setRole((String) charData.get("role"));
                
                // Handle nested appearance object or string
                Object appearanceObj = charData.get("appearance");
                if (appearanceObj instanceof String) {
                    character.setAppearance((String) appearanceObj);
                } else if (appearanceObj instanceof Map) {
                    try {
                        character.setAppearance(objectMapper.writeValueAsString(appearanceObj));
                    } catch (Exception e) {
                        character.setAppearance(appearanceObj.toString());
                    }
                } else if (appearanceObj != null) {
                    character.setAppearance(appearanceObj.toString());
                }
                
                character.setPersonality((String) charData.get("personality"));
                character.setDescription((String) charData.get("description"));
                character = characterRepository.save(character);
            } else if (character.getImageUrl() == null || character.getImageUrl().isEmpty()) {
                // Update existing character if no image
                character.setRole((String) charData.get("role"));
                
                Object appearanceObj = charData.get("appearance");
                if (appearanceObj instanceof String) {
                    character.setAppearance((String) appearanceObj);
                } else if (appearanceObj instanceof Map) {
                    try {
                        character.setAppearance(objectMapper.writeValueAsString(appearanceObj));
                    } catch (Exception e) {
                        character.setAppearance(appearanceObj.toString());
                    }
                } else if (appearanceObj != null) {
                    character.setAppearance(appearanceObj.toString());
                }
                
                character.setPersonality((String) charData.get("personality"));
                character.setDescription((String) charData.get("description"));
                character = characterRepository.save(character);
            }
            // If character exists AND has image, we keep it as is (preserve image).
            
            if (episode != null) {
                // Link to episode
                if (episode.getCharacters() == null) {
                    episode.setCharacters(new ArrayList<>());
                }
                if (!episode.getCharacters().contains(character)) {
                    episode.getCharacters().add(character);
                    episodeRepository.save(episode);
                }
            }
            
            savedChars.add(character);
        }
        return savedChars;
    }

    @Override
    @Async
    public void batchGenerateCharacterImages(List<Long> characterIds, String model) {
        for (Long characterId : characterIds) {
            try {
                // Call generateCharacterImage logic (without transactional here if handled inside)
                // We can't call self.generateCharacterImage because it returns ImageGeneration and is synchronous.
                // We should just call it directly or via self if we want transactional?
                // generateCharacterImage is @Transactional. Calling it via self will start new transaction if propagation allows.
                // But we are already in async method (if called via proxy).
                
                // Wait, generateCharacterImage is NOT @Async.
                // If batchGenerateCharacterImages is @Async, it runs in background.
                // We can call generateCharacterImage sequentially.
                
                Character character = characterRepository.findById(characterId).orElse(null);
                if (character == null) continue;
                
                // Check if already has image? Frontend might allow regenerating.
                
                self.generateCharacterImage(characterId, model, null);
                
            } catch (Exception e) {
                log.error("Failed to generate image for character " + characterId, e);
            }
        }
    }

    @Override
    public ImageGeneration generateCharacterImage(Long characterId, String model, String style) {
        // Use TransactionTemplate to read character data including lazy associations
        GenerateImageRequest req = transactionTemplate.execute(status -> {
            Character character = characterRepository.findById(characterId)
                    .orElseThrow(() -> new RuntimeException("Character not found"));
            
            String prompt = character.getAppearance();
            if (prompt == null || prompt.isEmpty()) {
                prompt = character.getDescription();
            }
            if (prompt == null || prompt.isEmpty()) {
                prompt = character.getName();
            }
            
            // Access drama to ensure it's loaded if lazy
            Drama drama = character.getDrama();
            if (drama != null && style != null && !style.isEmpty()) {
                prompt += ", " + style;
            } else if (drama != null && drama.getStyle() != null) {
                prompt += ", " + drama.getStyle();
            }
            
            GenerateImageRequest request = new GenerateImageRequest();
            request.setDramaId(String.valueOf(drama.getId()));
            request.setCharacterId(character.getId());
            request.setImageType("character");
            request.setPrompt(prompt);
            request.setModel(model);
            request.setSize("1024x1024");
            
            return request;
        });
        
        return imageGenerationService.generateImage(req);
    }

    @Override
    public List<Character> listCharacters(Long dramaId) {
        return characterRepository.findByDramaId(dramaId);
    }

    @Override
    public Character getCharacter(Long id) {
        return characterRepository.findById(id).orElseThrow(() -> new RuntimeException("Character not found"));
    }

    @Override
    public Character updateCharacter(Long id, Character updates) {
        Character charData = getCharacter(id);
        if (updates.getName() != null) charData.setName(updates.getName());
        if (updates.getRole() != null) charData.setRole(updates.getRole());
        if (updates.getAppearance() != null) charData.setAppearance(updates.getAppearance());
        if (updates.getDescription() != null) charData.setDescription(updates.getDescription());
        if (updates.getImageUrl() != null) charData.setImageUrl(updates.getImageUrl());
        return characterRepository.save(charData);
    }

    @Override
    public void deleteCharacter(Long id) {
        characterRepository.deleteById(id);
    }
}
