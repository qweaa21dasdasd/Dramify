package com.dramagenerator.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAIConfigRequest {
    @NotBlank
    @JsonProperty("service_type")
    private String serviceType;
    
    @NotBlank
    private String provider;
    
    @NotBlank
    private String name;
    
    @NotBlank
    @JsonProperty("base_url")
    private String baseUrl;
    
    @NotBlank
    @JsonProperty("api_key")
    private String apiKey;
    
    private Object model;
    
    private Integer priority;
    
    @JsonProperty("is_active")
    private Boolean isActive;
}
