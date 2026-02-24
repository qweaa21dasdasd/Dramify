package com.dramagenerator.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AIServiceConfigDTO {
    private Long id;
    
    @JsonProperty("service_type")
    private String serviceType;
    
    private String provider;
    private String name;
    
    @JsonProperty("base_url")
    private String baseUrl;
    
    @JsonProperty("api_key")
    private String apiKey;
    
    private Object model; // List<String> or String
    
    private Integer priority;
    
    @JsonProperty("is_default")
    private Boolean isDefault;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
