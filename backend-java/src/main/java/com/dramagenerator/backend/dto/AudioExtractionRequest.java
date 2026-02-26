package com.dramagenerator.backend.dto;

import lombok.Data;

@Data
public class AudioExtractionRequest {
    private String videoUrl;
    private String videoPath;
    private Long dramaId;
    private Long episodeId;
    private String fileName;
}
