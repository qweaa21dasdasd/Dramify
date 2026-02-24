package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.GenerateImageRequest;
import com.dramagenerator.backend.model.Drama;
import com.dramagenerator.backend.model.ImageGeneration;
import com.dramagenerator.backend.repository.DramaRepository;
import com.dramagenerator.backend.repository.ImageGenerationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationServiceImpl implements ImageGenerationService {

    private final ImageGenerationRepository imageGenerationRepository;
    private final DramaRepository dramaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ImageGeneration generateImage(GenerateImageRequest request) {
        Long dramaId = Long.parseLong(request.getDramaId());
        Drama drama = dramaRepository.findById(dramaId)
                .orElseThrow(() -> new RuntimeException("Drama not found"));
        
        ImageGeneration imageGen = new ImageGeneration();
        imageGen.setDrama(drama);
        // Set other fields based on request...
        // Assuming setters exist for storyboardId, sceneId, etc. which are entities in the model
        // We need repositories to fetch them if we want to set the relationships
        
        imageGen.setPrompt(request.getPrompt());
        imageGen.setProvider(request.getProvider() != null ? request.getProvider() : "openai");
        imageGen.setStatus("pending");
        
        // Save to DB
        imageGen = imageGenerationRepository.save(imageGen);
        
        // Trigger async generation (placeholder)
        // processImageGeneration(imageGen.getId());
        
        return imageGen;
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
        // Implementation would use Specification and Pageable
        // For now returning empty list or simple find
        if (dramaId != null) {
            return imageGenerationRepository.findByDramaId(dramaId);
        }
        return List.of();
    }
}
