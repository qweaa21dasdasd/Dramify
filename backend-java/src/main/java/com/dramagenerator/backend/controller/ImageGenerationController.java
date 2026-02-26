package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.common.ApiResponse;
import com.dramagenerator.backend.dto.GenerateImageRequest;
import com.dramagenerator.backend.dto.PaginatedResponse;
import com.dramagenerator.backend.model.ImageGeneration;
import com.dramagenerator.backend.service.ImageGenerationService;
import com.dramagenerator.backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/image-generations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImageGenerationController {

    private final ImageGenerationService imageGenerationService;
    private final StorageService storageService;

    @PostMapping
    public ResponseEntity<ApiResponse<ImageGeneration>> generateImage(@RequestBody GenerateImageRequest request) {
        ImageGeneration imageGen = imageGenerationService.generateImage(request);
        return ResponseEntity.ok(ApiResponse.success(imageGen));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageGeneration>> getImageGeneration(@PathVariable Long id) {
        ImageGeneration imageGen = imageGenerationService.getImageGeneration(id);
        return ResponseEntity.ok(ApiResponse.success(imageGen));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImageGeneration>>> listImageGenerations(
            @RequestParam(required = false) Long dramaId,
            @RequestParam(required = false) Long sceneId,
            @RequestParam(required = false) Long storyboardId,
            @RequestParam(required = false) String frameType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        List<ImageGeneration> list = imageGenerationService.listImageGenerations(dramaId, sceneId, storyboardId, frameType, status, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(@RequestParam("file") MultipartFile file) {
        String path = storageService.store(file, "uploads");
        // Construct URL - simplistic approach
        // Ideally this should be handled by a storage configuration or a dedicated method to get public URL
        String url = "/static/" + path; 
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "url", url,
            "path", path
        )));
    }
}
