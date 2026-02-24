package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.dto.AIServiceConfigDTO;
import com.dramagenerator.backend.dto.CreateAIConfigRequest;
import com.dramagenerator.backend.dto.TestConnectionRequest;
import com.dramagenerator.backend.dto.UpdateAIConfigRequest;
import com.dramagenerator.backend.service.AIServiceConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-configs")
@RequiredArgsConstructor
public class AIServiceConfigController {

    private final AIServiceConfigService configService;

    @GetMapping
    public ResponseEntity<List<AIServiceConfigDTO>> listConfigs(@RequestParam(required = false, name = "service_type") String serviceType) {
        return ResponseEntity.ok(configService.listConfigs(serviceType));
    }

    @PostMapping
    public ResponseEntity<AIServiceConfigDTO> createConfig(@RequestBody @Validated CreateAIConfigRequest request) {
        return ResponseEntity.ok(configService.createConfig(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AIServiceConfigDTO> getConfig(@PathVariable Long id) {
        return ResponseEntity.ok(configService.getConfig(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AIServiceConfigDTO> updateConfig(@PathVariable Long id, @RequestBody @Validated UpdateAIConfigRequest request) {
        return ResponseEntity.ok(configService.updateConfig(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Void> testConnection(@RequestBody @Validated TestConnectionRequest request) {
        configService.testConnection(request);
        return ResponseEntity.ok().build();
    }
}
