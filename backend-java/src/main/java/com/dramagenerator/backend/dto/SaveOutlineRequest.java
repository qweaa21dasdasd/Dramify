package com.dramagenerator.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class SaveOutlineRequest {
    @NotBlank
    private String title;
    
    @NotBlank
    private String summary;
    
    private String genre;
    private List<String> tags;
}
