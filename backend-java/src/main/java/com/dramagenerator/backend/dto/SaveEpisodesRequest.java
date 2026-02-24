package com.dramagenerator.backend.dto;

import com.dramagenerator.backend.model.Episode;
import lombok.Data;
import java.util.List;

@Data
public class SaveEpisodesRequest {
    private List<Episode> episodes;
}
