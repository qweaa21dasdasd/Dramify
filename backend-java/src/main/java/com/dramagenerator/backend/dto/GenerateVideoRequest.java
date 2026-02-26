package com.dramagenerator.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class GenerateVideoRequest {
    private Long storyboardId;
    private String dramaId;
    private Long imageGenId;
    
    private String referenceMode; // single, first_last, multiple, none
    
    private String imageUrl;
    private String imageLocalPath;
    
    private String firstFrameUrl;
    private String firstFrameLocalPath;
    private String lastFrameUrl;
    private String lastFrameLocalPath;
    
    private List<String> referenceImageUrls;
    
    private String prompt;
    private String provider;
    private String model;
    private Integer duration;
    private Integer fps;
    private String aspectRatio;
    private String style;
    private Integer motionLevel;
    private String cameraMotion;
    private Long seed;
}
