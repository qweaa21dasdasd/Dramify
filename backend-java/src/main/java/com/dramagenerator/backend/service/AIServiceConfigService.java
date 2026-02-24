package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.AIServiceConfigDTO;
import com.dramagenerator.backend.dto.CreateAIConfigRequest;
import com.dramagenerator.backend.dto.TestConnectionRequest;
import com.dramagenerator.backend.dto.UpdateAIConfigRequest;
import java.util.List;

public interface AIServiceConfigService {
    List<AIServiceConfigDTO> listConfigs(String serviceType);
    AIServiceConfigDTO createConfig(CreateAIConfigRequest request);
    AIServiceConfigDTO getConfig(Long id);
    AIServiceConfigDTO updateConfig(Long id, UpdateAIConfigRequest request);
    void deleteConfig(Long id);
    void testConnection(TestConnectionRequest request);
}
