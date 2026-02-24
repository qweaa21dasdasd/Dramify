package com.dramagenerator.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

@Data
public class SaveProgressRequest {
    @NotBlank
    private String currentStep;
    
    private Map<String, Object> stepData;
}
