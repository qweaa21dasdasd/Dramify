package com.dramagenerator.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenerateCharactersRequest {
    @JsonProperty("drama_id")
    private Long dramaId;

    @JsonProperty("episode_id")
    private Long episodeId;

    private String outline;

    private Integer count;

    private Double temperature;

    private String model;
}
