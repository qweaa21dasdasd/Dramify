package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.common.ApiResponse;
import com.dramagenerator.backend.dto.GenerateImageRequest;
import com.dramagenerator.backend.model.ImageGeneration;
import com.dramagenerator.backend.model.Scene;
import com.dramagenerator.backend.service.ImageGenerationService;
import com.dramagenerator.backend.service.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImageController {

    private final SceneService sceneService;
    private final ImageGenerationService imageGenerationService;

    @GetMapping("/episode/{episodeId}/backgrounds")
    public ResponseEntity<ApiResponse<List<Scene>>> getBackgrounds(@PathVariable Long episodeId) {
        return ResponseEntity.ok(ApiResponse.success(sceneService.listScenes(episodeId)));
    }

    @PostMapping("/episode/{episodeId}/backgrounds/extract")
    public ResponseEntity<ApiResponse<Map<String, String>>> extractBackgrounds(
            @PathVariable Long episodeId,
            @RequestBody(required = false) Map<String, String> request
    ) {
        String model = request != null ? request.get("model") : null;
        String style = request != null ? request.get("style") : null;
        
        String taskId = sceneService.extractBackgroundsFromScript(episodeId, model, style);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "task_id", taskId,
            "status", "pending",
            "message", "Scene extraction task created"
        )));
    }

    @PostMapping("/episode/{episodeId}/batch")
    public ResponseEntity<ApiResponse<Map<String, String>>> batchGenerateBackgrounds(@PathVariable Long episodeId) {
        sceneService.batchGenerateScenes(episodeId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Batch generation started")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImageGeneration>> generateSingleBackground(@RequestBody Map<String, Object> request) {
        GenerateImageRequest genReq = new GenerateImageRequest();
        
        if (request.containsKey("drama_id")) {
            genReq.setDramaId(request.get("drama_id").toString());
        }
        
        if (request.containsKey("background_id")) {
            genReq.setSceneId(Long.parseLong(request.get("background_id").toString()));
            genReq.setImageType("scene");
        }
        
        if (request.containsKey("prompt")) {
            genReq.setPrompt((String) request.get("prompt"));
        }
        
        // Defaults
        genReq.setSize("1024x1024");
        genReq.setQuality("standard");
        genReq.setProvider("openai"); // Default
        
        ImageGeneration imageGen = imageGenerationService.generateImage(genReq);
        return ResponseEntity.ok(ApiResponse.success(imageGen));
    }
}
