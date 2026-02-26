package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.common.ApiResponse;
import com.dramagenerator.backend.dto.GenerateCharactersRequest;
import com.dramagenerator.backend.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/generation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GenerationController {

    private final CharacterService characterService;

    @PostMapping("/characters")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateCharacters(@RequestBody GenerateCharactersRequest request) {
        String taskId = characterService.extractCharacters(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "task_id", taskId,
            "status", "pending",
            "message", "Character extraction task created"
        )));
    }
}
