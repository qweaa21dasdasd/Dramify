package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.FinalizeEpisodeRequest;
import com.dramagenerator.backend.model.VideoMerge;
import java.util.List;
import java.util.Map;

public interface VideoMergeService {
    VideoMerge mergeVideos(FinalizeEpisodeRequest request);
    VideoMerge getVideoMerge(Long id);
    List<VideoMerge> listVideoMerges(Long episodeId, String status, int page, int pageSize);
    Map<String, Object> finalizeEpisode(String episodeId, FinalizeEpisodeRequest request);
}
