package com.dramagenerator.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class FinalizeEpisodeRequest {
    private String episodeId;
    private List<TimelineClip> clips;

    @Data
    public static class TimelineClip {
        private Object assetId; // Can be String or Number
        private String storyboardId;
        private int order;
        private Double startTime;
        private Double endTime;
        private Double duration;
        private Map<String, Object> transition;
    }
}
