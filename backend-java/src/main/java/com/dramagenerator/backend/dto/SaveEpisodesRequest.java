package com.dramagenerator.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class SaveEpisodesRequest {
    private List<EpisodeDTO> episodes;

    @Data
    public static class EpisodeDTO {
        @JsonProperty("episode_number")
        private Integer episodeNumber;
        
        private String title;
        private String description;
        
        @JsonProperty("script_content")
        private String scriptContent;
        
        private Integer duration;
    }
}
