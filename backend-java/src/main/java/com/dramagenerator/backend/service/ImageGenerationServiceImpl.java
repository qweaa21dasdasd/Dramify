package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.GenerateImageRequest;
import com.dramagenerator.backend.model.*;
import com.dramagenerator.backend.model.Character;
import com.dramagenerator.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationServiceImpl implements ImageGenerationService {

    private final ImageGenerationRepository imageGenerationRepository;
    private final DramaRepository dramaRepository;
    private final StoryboardRepository storyboardRepository;
    private final SceneRepository sceneRepository;
    private final CharacterRepository characterRepository;
    private final PropRepository propRepository;
    private final AIService aiService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private ImageGenerationService self;

    @Override
    public ImageGeneration generateImage(GenerateImageRequest request) {
        Long dramaId = Long.parseLong(request.getDramaId());
        Drama drama = dramaRepository.findById(dramaId)
                .orElseThrow(() -> new RuntimeException("Drama not found"));
        
        ImageGeneration imageGen = new ImageGeneration();
        imageGen.setDrama(drama);
        imageGen.setPrompt(request.getPrompt());
        imageGen.setNegativePrompt(request.getNegativePrompt());
        imageGen.setProvider(request.getProvider() != null ? request.getProvider() : "openai");
        imageGen.setModel(request.getModel());
        imageGen.setSize(request.getSize());
        imageGen.setQuality(request.getQuality());
        imageGen.setStyle(request.getStyle());
        imageGen.setSteps(request.getSteps());
        imageGen.setCfgScale(request.getCfgScale());
        imageGen.setSeed(request.getSeed());
        imageGen.setWidth(request.getWidth());
        imageGen.setHeight(request.getHeight());
        imageGen.setLocalPath(request.getImageLocalPath());
        imageGen.setStatus("pending");
        imageGen.setImageType(request.getImageType() != null ? request.getImageType() : "storyboard");
        imageGen.setFrameType(request.getFrameType());
        
        if (request.getStoryboardId() != null) {
            storyboardRepository.findById(request.getStoryboardId()).ifPresent(imageGen::setStoryboard);
        }
        if (request.getSceneId() != null) {
            sceneRepository.findById(request.getSceneId()).ifPresent(imageGen::setScene);
        }
        if (request.getCharacterId() != null) {
            characterRepository.findById(request.getCharacterId()).ifPresent(imageGen::setCharacter);
        }
        if (request.getPropId() != null) {
            propRepository.findById(request.getPropId()).ifPresent(imageGen::setProp);
        }
        
        try {
            if (request.getReferenceImages() != null) {
                imageGen.setReferenceImages(objectMapper.writeValueAsString(request.getReferenceImages()));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize reference images", e);
        }
        
        // Save to DB
        imageGen = imageGenerationRepository.save(imageGen);
        
        // Trigger async generation
        if (self != null) {
            self.processImageGeneration(imageGen.getId());
        } else {
            // Fallback for non-proxy invocation (sync)
            processImageGeneration(imageGen.getId());
        }
        
        return imageGen;
    }

    @Override
    @Async
    public void processImageGeneration(Long imageGenId) {
        ImageGeneration imageGen = imageGenerationRepository.findById(imageGenId).orElse(null);
        if (imageGen == null) return;
        
        try {
            imageGen.setStatus("processing");
            imageGenerationRepository.save(imageGen);
            
            Map<String, Object> options = new HashMap<>();
            if (imageGen.getModel() != null) options.put("model", imageGen.getModel());
            if (imageGen.getSize() != null) options.put("size", imageGen.getSize());
            if (imageGen.getQuality() != null) options.put("quality", imageGen.getQuality());
            if (imageGen.getStyle() != null) options.put("style", imageGen.getStyle());
            
            // Generate Image
            String imageUrl = aiService.generateImage(imageGen.getPrompt(), options);
            
            // Download and store locally
            String localPath = null;
            try {
                localPath = storageService.storeFromUrl(imageUrl, "images");
            } catch (Exception e) {
                log.warn("Failed to download image to local storage", e);
            }
            
            // Update ImageGeneration record
            imageGen.setStatus("completed");
            imageGen.setImageUrl(imageUrl);
            imageGen.setLocalPath(localPath);
            imageGen.setCompletedAt(LocalDateTime.now());
            imageGenerationRepository.save(imageGen);
            
            // Update related entities
            updateRelatedEntities(imageGen);
            
        } catch (Exception e) {
            log.error("Image generation failed", e);
            imageGen.setStatus("failed");
            imageGen.setErrorMsg(e.getMessage());
            imageGenerationRepository.save(imageGen);
        }
    }

    private void updateRelatedEntities(ImageGeneration imageGen) {
        String finalUrl = imageGen.getImageUrl();
        // Prefer local path (served via static URL) if available? 
        // Or keep remote URL? Usually remote URL expires for DALL-E.
        // Let's assume frontend can access local path via /static/...
        // But for now let's store the URL we have.
        
        // Actually, if localPath is present, we might want to construct a full URL for it?
        // Or just store the relative path and let frontend handle it.
        // Go version updates `image_url` with remote URL and `local_path` with relative path.
        
        if (imageGen.getStoryboard() != null) {
            storyboardRepository.findById(imageGen.getStoryboard().getId()).ifPresent(sb -> {
                sb.setComposedImage(finalUrl);
                storyboardRepository.save(sb);
            });
        }
        
        if (imageGen.getScene() != null && "scene".equals(imageGen.getImageType())) {
            sceneRepository.findById(imageGen.getScene().getId()).ifPresent(scene -> {
                scene.setImageUrl(finalUrl);
                scene.setStatus("generated");
                sceneRepository.save(scene);
            });
        }
        
        if (imageGen.getCharacter() != null) {
            characterRepository.findById(imageGen.getCharacter().getId()).ifPresent(c -> {
                c.setImageUrl(finalUrl);
                characterRepository.save(c);
            });
        }
        
        if (imageGen.getProp() != null) {
            propRepository.findById(imageGen.getProp().getId()).ifPresent(p -> {
                p.setImageUrl(finalUrl);
                propRepository.save(p);
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ImageGeneration getImageGeneration(Long id) {
        return imageGenerationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image generation not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageGeneration> listImageGenerations(Long dramaId, Long sceneId, Long storyboardId, String frameType, String status, int page, int pageSize) {
        // Simple implementation for now
        if (dramaId != null) {
            return imageGenerationRepository.findByDramaId(dramaId);
        }
        return List.of();
    }
}
