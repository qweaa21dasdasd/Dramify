package com.dramagenerator.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestConnectionRequest {
    @NotBlank
    @JsonProperty("base_url")
    private String baseUrl;
    
    @NotBlank
    @JsonProperty("api_key")
    private String apiKey;
    
    private Object model;
    
    private String provider;
}
