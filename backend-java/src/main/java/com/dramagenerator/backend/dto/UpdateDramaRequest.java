package com.dramagenerator.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDramaRequest {
    @Size(min = 1, max = 100)
    private String title;
    
    private String description;
    private String genre;
    private String style;
    private String tags;
    private String status;
}
