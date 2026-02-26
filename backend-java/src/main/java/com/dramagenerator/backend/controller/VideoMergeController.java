package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.common.ApiResponse;
import com.dramagenerator.backend.dto.FinalizeEpisodeRequest;
import com.dramagenerator.backend.model.VideoMerge;
import com.dramagenerator.backend.service.VideoMergeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/video-merges")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VideoMergeController {

    private final VideoMergeService videoMergeService;

    @PostMapping("/finalize")
    public ResponseEntity<ApiResponse<Map<String, Object>>> finalizeEpisode(@RequestBody FinalizeEpisodeRequest request) {
        Map<String, Object> result = videoMergeService.finalizeEpisode(request.getEpisodeId(), request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoMerge>> getVideoMerge(@PathVariable Long id) {
        VideoMerge merge = videoMergeService.getVideoMerge(id);
        return ResponseEntity.ok(ApiResponse.success(merge));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VideoMerge>>> listVideoMerges(
            @RequestParam(required = false) Long episodeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        List<VideoMerge> list = videoMergeService.listVideoMerges(episodeId, status, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
