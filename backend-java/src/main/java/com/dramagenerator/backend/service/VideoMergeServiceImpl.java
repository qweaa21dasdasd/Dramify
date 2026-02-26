package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.FinalizeEpisodeRequest;
import com.dramagenerator.backend.model.*;
import com.dramagenerator.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoMergeServiceImpl implements VideoMergeService {

    private final VideoMergeRepository videoMergeRepository;
    private final EpisodeRepository episodeRepository;
    private final DramaRepository dramaRepository;
    private final AssetRepository assetRepository;
    private final VideoGenerationRepository videoGenerationRepository;
    private final StoryboardRepository storyboardRepository;
    private final FFmpegService ffmpegService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public VideoMerge mergeVideos(FinalizeEpisodeRequest request) {
        // Find Episode
        Episode episode = episodeRepository.findById(Long.parseLong(request.getEpisodeId()))
                .orElseThrow(() -> new RuntimeException("Episode not found"));
        
        Drama drama = episode.getDrama();
        
        VideoMerge merge = new VideoMerge();
        merge.setEpisode(episode);
        merge.setDrama(drama);
        merge.setTitle(drama.getTitle() + " - 第" + episode.getEpisodeNumber() + "集");
        merge.setProvider("ffmpeg"); // Local merge
        merge.setStatus("pending");
        
        try {
            merge.setScenes(objectMapper.writeValueAsString(request.getClips()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize clips", e);
        }
        
        merge = videoMergeRepository.save(merge);
        
        // Async process
        processMerge(merge.getId());
        
        return merge;
    }

    @Async
    public void processMerge(Long mergeId) {
        VideoMerge merge = videoMergeRepository.findById(mergeId).orElse(null);
        if (merge == null) return;
        
        try {
            merge.setStatus("processing");
            videoMergeRepository.save(merge);
            
            // Parse clips
            List<FinalizeEpisodeRequest.TimelineClip> clips = objectMapper.readValue(
                    merge.getScenes(), 
                    new TypeReference<List<FinalizeEpisodeRequest.TimelineClip>>() {});
            
            List<String> videoPaths = new ArrayList<>();
            List<Integer> skippedScenes = new ArrayList<>();
            
            for (FinalizeEpisodeRequest.TimelineClip clip : clips) {
                String videoPath = resolveVideoPath(clip);
                if (videoPath != null && new File(videoPath).exists()) {
                    videoPaths.add(videoPath);
                } else {
                    log.warn("Video not found for clip: {}", clip);
                    if (clip.getStoryboardId() != null) {
                        try {
                            skippedScenes.add(Integer.parseInt(clip.getStoryboardId())); // Assuming storyboardId is number? 
                            // Or use order/index
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }
            }
            
            if (videoPaths.isEmpty()) {
                throw new RuntimeException("No valid video clips found to merge");
            }
            
            // Merge using FFmpeg
            String outputDir = storageService.getAbsolutePath("videos/merged");
            String mergedPath = ffmpegService.mergeVideos(videoPaths, outputDir);
            
            // Calculate duration
            double duration = ffmpegService.getVideoDuration(mergedPath);
            
            // Update Merge Record
            // Need relative path for URL access via static handler
            // mergedPath is absolute. storageService root is base.
            // Assuming storageService.store... logic handles uploads, here we generated file directly in storage.
            // We need to convert absolute path to relative path "videos/merged/filename.mp4"
            
            String relativePath = getRelativePath(mergedPath);
            // URL assumed to be static served
            String mergedUrl = "/static/" + relativePath;
            
            merge.setStatus("completed");
            merge.setMergedUrl(mergedUrl); // Or full URL if needed
            merge.setDuration((int) duration);
            merge.setCompletedAt(LocalDateTime.now());
            videoMergeRepository.save(merge);
            
            // Update Episode
            Episode episode = merge.getEpisode();
            episode.setStatus("completed");
            episode.setVideoUrl(mergedUrl);
            episodeRepository.save(episode);
            
        } catch (Exception e) {
            log.error("Video merge failed", e);
            merge.setStatus("failed");
            merge.setErrorMsg(e.getMessage());
            videoMergeRepository.save(merge);
        }
    }
    
    private String getRelativePath(String absolutePath) {
        // Simplistic logic: assume storage root is part of path
        // Ideally pass root from config or storage service
        // storageService.getAbsolutePath("") returns root.
        String root = storageService.getAbsolutePath("");
        if (absolutePath.startsWith(root)) {
            String rel = absolutePath.substring(root.length());
            if (rel.startsWith(File.separator)) rel = rel.substring(1);
            return rel.replace("\\", "/");
        }
        return new File(absolutePath).getName(); // Fallback
    }

    private String resolveVideoPath(FinalizeEpisodeRequest.TimelineClip clip) {
        // Priority: Asset -> VideoGeneration -> Storyboard URL
        
        // 1. Asset
        if (clip.getAssetId() != null) {
            try {
                Long assetId = Long.parseLong(clip.getAssetId().toString());
                Asset asset = assetRepository.findById(assetId).orElse(null);
                if (asset != null && asset.getLocalPath() != null) {
                    return storageService.getAbsolutePath(asset.getLocalPath());
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        
        // 2. VideoGeneration (via Storyboard ID)
        if (clip.getStoryboardId() != null) {
            try {
                Long sbId = Long.parseLong(clip.getStoryboardId());
                // Find latest completed video generation for this storyboard
                List<VideoGeneration> gens = videoGenerationRepository.findByStoryboardId(sbId);
                // Filter completed and sort desc
                VideoGeneration gen = gens.stream()
                        .filter(g -> "completed".equals(g.getStatus()))
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // Desc
                        .findFirst()
                        .orElse(null);
                
                if (gen != null && gen.getLocalPath() != null) {
                    return storageService.getAbsolutePath(gen.getLocalPath());
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        
        // 3. Storyboard URL (download if needed? No, we need local path for ffmpeg)
        // If we only have URL, we must download it.
        // For now, skip if no local path found.
        
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public VideoMerge getVideoMerge(Long id) {
        return videoMergeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merge not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoMerge> listVideoMerges(Long episodeId, String status, int page, int pageSize) {
        // Simple implementation
        // Need custom query for dynamic filters
        return videoMergeRepository.findAll(); // TODO: Implement proper filtering
    }

    @Override
    @Transactional
    public Map<String, Object> finalizeEpisode(String episodeId, FinalizeEpisodeRequest request) {
        request.setEpisodeId(episodeId);
        VideoMerge merge = mergeVideos(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Video merge started");
        result.put("merge_id", merge.getId());
        result.put("episode_id", episodeId);
        return result;
    }
}
