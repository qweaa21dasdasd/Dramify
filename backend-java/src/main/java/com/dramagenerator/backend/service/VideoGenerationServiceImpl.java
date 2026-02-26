package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.GenerateVideoRequest;
import com.dramagenerator.backend.model.*;
import com.dramagenerator.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoGenerationServiceImpl implements VideoGenerationService {

    private final VideoGenerationRepository videoGenerationRepository;
    private final DramaRepository dramaRepository;
    private final StoryboardRepository storyboardRepository;
    private final ImageGenerationRepository imageGenerationRepository;
    private final AIService aiService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public VideoGeneration generateVideo(GenerateVideoRequest request) {
        Long dramaId = Long.parseLong(request.getDramaId());
        Drama drama = dramaRepository.findById(dramaId)
                .orElseThrow(() -> new RuntimeException("Drama not found"));
        
        VideoGeneration videoGen = new VideoGeneration();
        videoGen.setDrama(drama);
        videoGen.setPrompt(request.getPrompt());
        videoGen.setProvider(request.getProvider() != null ? request.getProvider() : "openai");
        videoGen.setModel(request.getModel());
        videoGen.setDuration(request.getDuration());
        videoGen.setFps(request.getFps());
        videoGen.setAspectRatio(request.getAspectRatio());
        videoGen.setStyle(request.getStyle());
        videoGen.setMotionLevel(request.getMotionLevel());
        videoGen.setCameraMotion(request.getCameraMotion());
        videoGen.setSeed(request.getSeed());
        videoGen.setReferenceMode(request.getReferenceMode());
        videoGen.setStatus("pending");
        
        if (request.getStoryboardId() != null) {
            storyboardRepository.findById(request.getStoryboardId()).ifPresent(videoGen::setStoryboard);
        }
        
        if (request.getImageGenId() != null) {
            imageGenerationRepository.findById(request.getImageGenId()).ifPresent(videoGen::setImageGen);
        }
        
        if (request.getImageUrl() != null) videoGen.setImageUrl(request.getImageUrl());
        if (request.getFirstFrameUrl() != null) videoGen.setFirstFrameUrl(request.getFirstFrameUrl());
        if (request.getLastFrameUrl() != null) videoGen.setLastFrameUrl(request.getLastFrameUrl());
        
        try {
            if (request.getReferenceImageUrls() != null) {
                videoGen.setReferenceImageUrls(objectMapper.writeValueAsString(request.getReferenceImageUrls()));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize reference images", e);
        }
        
        // Save to DB
        videoGen = videoGenerationRepository.save(videoGen);
        
        // Trigger async generation
        processVideoGeneration(videoGen.getId());
        
        return videoGen;
    }

    @Async
    public void processVideoGeneration(Long videoGenId) {
        VideoGeneration videoGen = videoGenerationRepository.findById(videoGenId).orElse(null);
        if (videoGen == null) return;
        
        try {
            videoGen.setStatus("processing");
            videoGenerationRepository.save(videoGen);
            
            Map<String, Object> options = new HashMap<>();
            if (videoGen.getModel() != null) options.put("model", videoGen.getModel());
            if (videoGen.getImageUrl() != null) options.put("image_url", videoGen.getImageUrl());
            
            // Generate Video
            String result = aiService.generateVideo(videoGen.getPrompt(), options);
            
            String videoUrl = null;
            if (result.startsWith("TASK:")) {
                // Handle task polling logic (simplified for now)
                videoGen.setTaskId(result.substring(5));
                // Polling should ideally be done in a separate scheduler or loop here
                // For MVP, assuming we update manually or implement polling later
                // Or just loop here
                log.info("Task ID returned: {}, polling logic not fully implemented", videoGen.getTaskId());
                // Simulate waiting or assume completed for test env
            } else {
                videoUrl = result;
            }
            
            if (videoUrl != null) {
                // Download and store locally
                String localPath = null;
                try {
                    localPath = storageService.storeFromUrl(videoUrl, "videos");
                } catch (Exception e) {
                    log.warn("Failed to download video to local storage", e);
                }
                
                // Update VideoGeneration record
                videoGen.setStatus("completed");
                videoGen.setVideoUrl(videoUrl);
                videoGen.setLocalPath(localPath);
                videoGen.setCompletedAt(LocalDateTime.now());
                videoGenerationRepository.save(videoGen);
                
                // Update related entities
                updateRelatedEntities(videoGen);
            } else {
                // If Task ID, keep processing or schedule poll
                videoGenerationRepository.save(videoGen);
            }
            
        } catch (Exception e) {
            log.error("Video generation failed", e);
            videoGen.setStatus("failed");
            videoGen.setErrorMsg(e.getMessage());
            videoGenerationRepository.save(videoGen);
        }
    }

    private void updateRelatedEntities(VideoGeneration videoGen) {
        String finalUrl = videoGen.getVideoUrl();
        
        if (videoGen.getStoryboard() != null) {
            storyboardRepository.findById(videoGen.getStoryboard().getId()).ifPresent(sb -> {
                sb.setVideoUrl(finalUrl);
                if (videoGen.getDuration() != null) {
                    sb.setDuration(videoGen.getDuration());
                }
                storyboardRepository.save(sb);
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VideoGeneration getVideoGeneration(Long id) {
        return videoGenerationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video generation not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoGeneration> listVideoGenerations(Long dramaId, Long storyboardId, String status, int page, int pageSize) {
        // Simple implementation
        if (dramaId != null) {
            return videoGenerationRepository.findByDramaId(dramaId);
        }
        return List.of();
    }
}
