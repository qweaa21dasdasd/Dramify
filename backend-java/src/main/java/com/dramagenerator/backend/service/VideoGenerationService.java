package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.GenerateVideoRequest;
import com.dramagenerator.backend.model.VideoGeneration;
import java.util.List;

public interface VideoGenerationService {
    VideoGeneration generateVideo(GenerateVideoRequest request);
    VideoGeneration getVideoGeneration(Long id);
    List<VideoGeneration> listVideoGenerations(Long dramaId, Long storyboardId, String status, int page, int pageSize);
}
