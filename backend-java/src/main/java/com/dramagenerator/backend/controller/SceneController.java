package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.common.ApiResponse;
import com.dramagenerator.backend.dto.GenerateImageRequest;
import com.dramagenerator.backend.service.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/scenes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SceneController {

    private final SceneService sceneService;

    @PostMapping("/generate-image")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateSceneImage(@RequestBody Map<String, Object> request) {
        Long sceneId = Long.valueOf(request.get("scene_id").toString());
        String model = (String) request.get("model");
        
        sceneService.generateSceneImage(sceneId, model);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "message", "Scene image generation started",
            "status", "pending"
        )));
    }
}
