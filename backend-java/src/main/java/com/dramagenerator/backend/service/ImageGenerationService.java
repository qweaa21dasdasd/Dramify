package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.GenerateImageRequest;
import com.dramagenerator.backend.model.ImageGeneration;
import java.util.List;

public interface ImageGenerationService {
    ImageGeneration generateImage(GenerateImageRequest request);
    ImageGeneration getImageGeneration(Long id);
    void processImageGeneration(Long imageGenId);
    List<ImageGeneration> listImageGenerations(Long dramaId, Long sceneId, Long storyboardId, String frameType, String status, int page, int pageSize);
}
