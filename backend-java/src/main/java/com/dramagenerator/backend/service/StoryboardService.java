package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.CreateStoryboardRequest;
import com.dramagenerator.backend.model.Storyboard;

import java.util.Map;

public interface StoryboardService {
    String generateStoryboard(Long episodeId, String model);
    void updateStoryboard(Long id, Map<String, Object> updates);
    Storyboard createStoryboard(CreateStoryboardRequest request);
    void deleteStoryboard(Long id);
}
