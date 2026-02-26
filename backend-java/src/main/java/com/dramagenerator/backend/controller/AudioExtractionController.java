package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.common.ApiResponse;
import com.dramagenerator.backend.dto.AudioExtractionRequest;
import com.dramagenerator.backend.service.AudioExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/audio-extraction")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AudioExtractionController {

    private final AudioExtractionService audioExtractionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> extractAudio(@RequestBody AudioExtractionRequest request) {
        Map<String, Object> result = audioExtractionService.extractAudio(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
