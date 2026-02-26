package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.common.ApiResponse;
import com.dramagenerator.backend.dto.GenerateVideoRequest;
import com.dramagenerator.backend.model.VideoGeneration;
import com.dramagenerator.backend.service.VideoGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/video-generations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VideoGenerationController {

    private final VideoGenerationService videoGenerationService;

    @PostMapping
    public ResponseEntity<ApiResponse<VideoGeneration>> generateVideo(@RequestBody GenerateVideoRequest request) {
        VideoGeneration videoGen = videoGenerationService.generateVideo(request);
        return ResponseEntity.ok(ApiResponse.success(videoGen));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoGeneration>> getVideoGeneration(@PathVariable Long id) {
        VideoGeneration videoGen = videoGenerationService.getVideoGeneration(id);
        return ResponseEntity.ok(ApiResponse.success(videoGen));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VideoGeneration>>> listVideoGenerations(
            @RequestParam(required = false) Long dramaId,
            @RequestParam(required = false) Long storyboardId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        List<VideoGeneration> list = videoGenerationService.listVideoGenerations(dramaId, storyboardId, status, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
