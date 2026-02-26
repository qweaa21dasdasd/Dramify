package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.common.ApiResponse;
import com.dramagenerator.backend.model.Character;
import com.dramagenerator.backend.model.ImageGeneration;
import com.dramagenerator.backend.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CharacterController {

    private final CharacterService characterService;

    @PostMapping("/dramas/{dramaId}/episodes/{episodeId}/extract-characters")
    public ResponseEntity<ApiResponse<Map<String, String>>> extractCharacters(@PathVariable Long dramaId, @PathVariable Long episodeId) {
        String taskId = characterService.extractCharactersFromScript(episodeId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("task_id", taskId)));
    }

    @PostMapping("/characters/batch-generate-images")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchGenerateCharacterImages(@RequestBody Map<String, Object> request) {
        List<String> idsStr = (List<String>) request.get("character_ids");
        String model = (String) request.get("model");
        
        List<Long> ids = idsStr.stream().map(Long::valueOf).toList();
        
        characterService.batchGenerateCharacterImages(ids, model);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "message", "Batch generation started",
            "count", ids.size()
        )));
    }

    @PostMapping("/characters/{id}/generate-image")
    public ResponseEntity<ApiResponse<ImageGeneration>> generateCharacterImage(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String model = request.get("model");
        String style = request.get("style");
        ImageGeneration imageGen = characterService.generateCharacterImage(id, model, style);
        return ResponseEntity.ok(ApiResponse.success(imageGen));
    }

    @PutMapping("/characters/{id}")
    public ResponseEntity<ApiResponse<Character>> updateCharacter(@PathVariable Long id, @RequestBody Character character) {
        return ResponseEntity.ok(ApiResponse.success(characterService.updateCharacter(id, character)));
    }

    @DeleteMapping("/characters/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCharacter(@PathVariable Long id) {
        characterService.deleteCharacter(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
