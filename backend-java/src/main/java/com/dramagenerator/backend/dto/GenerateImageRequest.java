package com.dramagenerator.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class GenerateImageRequest {
    private Long storyboardId;
    
    @NotBlank
    private String dramaId;
    
    private Long sceneId;
    private Long characterId;
    private Long propId;
    private String imageType;
    private String frameType;
    
    @NotBlank
    private String prompt;
    
    private String negativePrompt;
    private String provider;
    private String model;
    private String size;
    private String quality;
    private String style;
    private Integer steps;
    private Double cfgScale;
    private Long seed;
    private Integer width;
    private Integer height;
    private String imageLocalPath;
    private List<String> referenceImages;
}
