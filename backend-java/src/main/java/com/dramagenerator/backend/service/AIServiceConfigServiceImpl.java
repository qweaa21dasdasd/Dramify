package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.AIServiceConfigDTO;
import com.dramagenerator.backend.dto.CreateAIConfigRequest;
import com.dramagenerator.backend.dto.TestConnectionRequest;
import com.dramagenerator.backend.dto.UpdateAIConfigRequest;
import com.dramagenerator.backend.model.AIServiceConfig;
import com.dramagenerator.backend.repository.AIServiceConfigRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceConfigServiceImpl implements AIServiceConfigService {

    private final AIServiceConfigRepository configRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AIServiceConfigDTO> listConfigs(String serviceType) {
        List<AIServiceConfig> configs;
        if (StringUtils.hasText(serviceType)) {
            configs = configRepository.findByServiceType(serviceType);
        } else {
            configs = configRepository.findAll();
        }
        return configs.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AIServiceConfigDTO createConfig(CreateAIConfigRequest request) {
        AIServiceConfig config = new AIServiceConfig();
        config.setServiceType(request.getServiceType());
        config.setProvider(request.getProvider());
        config.setName(request.getName());
        config.setBaseUrl(request.getBaseUrl());
        config.setApiKey(request.getApiKey());
        config.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        config.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        config.setIsDefault(false); // Default logic can be added later

        try {
            if (request.getModel() != null) {
                config.setModel(objectMapper.writeValueAsString(request.getModel()));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize model", e);
            throw new RuntimeException("Invalid model format");
        }

        config = configRepository.save(config);
        return toDTO(config);
    }

    @Override
    @Transactional(readOnly = true)
    public AIServiceConfigDTO getConfig(Long id) {
        AIServiceConfig config = configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config not found"));
        return toDTO(config);
    }

    @Override
    @Transactional
    public AIServiceConfigDTO updateConfig(Long id, UpdateAIConfigRequest request) {
        AIServiceConfig config = configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config not found"));

        if (request.getName() != null) config.setName(request.getName());
        if (request.getProvider() != null) config.setProvider(request.getProvider());
        if (request.getBaseUrl() != null) config.setBaseUrl(request.getBaseUrl());
        if (request.getApiKey() != null) config.setApiKey(request.getApiKey());
        if (request.getPriority() != null) config.setPriority(request.getPriority());
        if (request.getIsActive() != null) config.setIsActive(request.getIsActive());

        try {
            if (request.getModel() != null) {
                config.setModel(objectMapper.writeValueAsString(request.getModel()));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize model", e);
            throw new RuntimeException("Invalid model format");
        }

        config = configRepository.save(config);
        return toDTO(config);
    }

    @Override
    @Transactional
    public void deleteConfig(Long id) {
        if (!configRepository.existsById(id)) {
            throw new RuntimeException("Config not found");
        }
        configRepository.deleteById(id);
    }

    @Override
    public void testConnection(TestConnectionRequest request) {
        // Implement connection test logic here
        // For now, just simulate success if fields are present
        if (!StringUtils.hasText(request.getBaseUrl()) || !StringUtils.hasText(request.getApiKey())) {
            throw new RuntimeException("Base URL and API Key are required");
        }
        // In a real implementation, make a simple HTTP request to the provider
    }

    private AIServiceConfigDTO toDTO(AIServiceConfig config) {
        AIServiceConfigDTO dto = new AIServiceConfigDTO();
        dto.setId(config.getId());
        dto.setServiceType(config.getServiceType());
        dto.setProvider(config.getProvider());
        dto.setName(config.getName());
        dto.setBaseUrl(config.getBaseUrl());
        dto.setApiKey(config.getApiKey());
        dto.setPriority(config.getPriority());
        dto.setIsDefault(config.getIsDefault());
        dto.setIsActive(config.getIsActive());
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());

        try {
            if (config.getModel() != null) {
                // Try to parse as List first, then String if it fails (though it should be JSON)
                // Assuming it's stored as JSON array or string
                // Ideally, we check the structure. For now, let's try to parse as generic Object
                dto.setModel(objectMapper.readValue(config.getModel(), Object.class));
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse model JSON for config id: " + config.getId(), e);
            dto.setModel(config.getModel()); // Fallback to raw string
        }

        return dto;
    }
}
