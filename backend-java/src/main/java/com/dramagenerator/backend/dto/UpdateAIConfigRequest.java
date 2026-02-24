package com.dramagenerator.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateAIConfigRequest {
    private String name;
    private String provider;
    
    @JsonProperty("base_url")
    private String baseUrl;
    
    @JsonProperty("api_key")
    private String apiKey;
    
    private Object model;
    
    private Integer priority;
    
    @JsonProperty("is_active")
    private Boolean isActive;
}
