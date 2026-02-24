package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.dto.*;
import com.dramagenerator.backend.model.Character;
import com.dramagenerator.backend.model.Drama;
import com.dramagenerator.backend.service.DramaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dramas")
@RequiredArgsConstructor
public class DramaController {

    private final DramaService dramaService;

    @PostMapping
    public ResponseEntity<Drama> createDrama(@RequestBody @Validated CreateDramaRequest request) {
        return ResponseEntity.ok(dramaService.createDrama(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Drama> getDrama(@PathVariable Long id) {
        return ResponseEntity.ok(dramaService.getDrama(id));
    }

    @GetMapping
    public ResponseEntity<Page<Drama>> listDramas(DramaListQuery query) {
        return ResponseEntity.ok(dramaService.listDramas(query));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Drama> updateDrama(@PathVariable Long id, @RequestBody @Validated UpdateDramaRequest request) {
        return ResponseEntity.ok(dramaService.updateDrama(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrama(@PathVariable Long id) {
        dramaService.deleteDrama(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDramaStats() {
        return ResponseEntity.ok(dramaService.getDramaStats());
    }

    @PostMapping("/{id}/outline")
    public ResponseEntity<Void> saveOutline(@PathVariable Long id, @RequestBody @Validated SaveOutlineRequest request) {
        dramaService.saveOutline(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/characters")
    public ResponseEntity<List<Character>> getCharacters(@PathVariable Long id, @RequestParam(required = false) Long episodeId) {
        return ResponseEntity.ok(dramaService.getCharacters(id, episodeId));
    }

    @PostMapping("/{id}/characters")
    public ResponseEntity<Void> saveCharacters(@PathVariable Long id, @RequestBody @Validated SaveCharactersRequest request) {
        dramaService.saveCharacters(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/episodes")
    public ResponseEntity<Void> saveEpisodes(@PathVariable Long id, @RequestBody @Validated SaveEpisodesRequest request) {
        dramaService.saveEpisodes(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<Void> saveProgress(@PathVariable Long id, @RequestBody @Validated SaveProgressRequest request) {
        dramaService.saveProgress(id, request);
        return ResponseEntity.ok().build();
    }
}
