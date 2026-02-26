package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.common.ApiResponse;
import com.dramagenerator.backend.dto.CreateStoryboardRequest;
import com.dramagenerator.backend.model.Storyboard;
import com.dramagenerator.backend.service.StoryboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StoryboardController {

    private final StoryboardService storyboardService;

    @PostMapping("/episodes/{episodeId}/storyboards")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateStoryboardShort(
            @PathVariable Long episodeId,
            @RequestBody(required = false) Map<String, Object> request) {
        String model = request != null ? (String) request.get("model") : null;
        
        String taskId = storyboardService.generateStoryboard(episodeId, model);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "task_id", taskId,
            "status", "pending",
            "message", "Storyboard generation task created"
        )));
    }

    @PostMapping("/dramas/{dramaId}/episodes/{episodeId}/storyboards/generate")
    public ResponseEntity<?> generateStoryboard(
            @PathVariable Long dramaId,
            @PathVariable Long episodeId,
            @RequestBody Map<String, Object> request) {
        String model = (String) request.get("model");
        
        // Ensure episode belongs to drama if needed
        
        String taskId = storyboardService.generateStoryboard(episodeId, model);
        
        Map<String, String> response = new HashMap<>();
        response.put("task_id", taskId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/storyboards")
    public ResponseEntity<Storyboard> createStoryboard(@RequestBody CreateStoryboardRequest request) {
        return ResponseEntity.ok(storyboardService.createStoryboard(request));
    }

    @PutMapping("/storyboards/{id}")
    public ResponseEntity<?> updateStoryboard(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        storyboardService.updateStoryboard(id, updates);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/storyboards/{id}")
    public ResponseEntity<?> deleteStoryboard(@PathVariable Long id) {
        storyboardService.deleteStoryboard(id);
        return ResponseEntity.ok().build();
    }
}
