package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.AudioExtractionRequest;
import com.dramagenerator.backend.model.Asset;
import com.dramagenerator.backend.model.Drama;
import com.dramagenerator.backend.model.Episode;
import com.dramagenerator.backend.repository.AssetRepository;
import com.dramagenerator.backend.repository.DramaRepository;
import com.dramagenerator.backend.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioExtractionServiceImpl implements AudioExtractionService {

    private final FFmpegService ffmpegService;
    private final StorageService storageService;
    private final AssetRepository assetRepository;
    private final DramaRepository dramaRepository;
    private final EpisodeRepository episodeRepository;

    @Override
    public Map<String, Object> extractAudio(AudioExtractionRequest request) {
        String videoPath = null;
        if (request.getVideoPath() != null) {
            videoPath = storageService.getAbsolutePath(request.getVideoPath());
        } else if (request.getVideoUrl() != null) {
            // Download to temp file
            videoPath = storageService.storeFromUrl(request.getVideoUrl(), "temp_videos");
            videoPath = storageService.getAbsolutePath(videoPath);
        } else {
            throw new RuntimeException("Video path or URL required");
        }

        try {
            String outputDir = storageService.getAbsolutePath("audio/extracted");
            String audioPath = ffmpegService.extractAudio(videoPath, outputDir);
            
            // Generate relative path for URL
            String relativePath = getRelativePath(audioPath);
            String audioUrl = "/static/" + relativePath;
            
            // Save as Asset if dramaId provided
            if (request.getDramaId() != null) {
                Drama drama = dramaRepository.findById(request.getDramaId()).orElse(null);
                if (drama != null) {
                    Asset asset = new Asset();
                    asset.setDrama(drama);
                    asset.setType("audio");
                    asset.setName(request.getFileName() != null ? request.getFileName() : "extracted_audio.mp3");
                    asset.setUrl(audioUrl);
                    asset.setLocalPath(relativePath);
                    // asset.setDuration(...) // Need to get duration
                    
                    if (request.getEpisodeId() != null) {
                        Episode episode = episodeRepository.findById(request.getEpisodeId()).orElse(null);
                        if (episode != null) {
                            asset.setEpisode(episode);
                        }
                    }
                    
                    assetRepository.save(asset);
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("url", audioUrl);
            result.put("path", relativePath);
            return result;
            
        } catch (Exception e) {
            log.error("Failed to extract audio", e);
            throw new RuntimeException("Failed to extract audio: " + e.getMessage());
        }
    }
    
    private String getRelativePath(String absolutePath) {
        String root = storageService.getAbsolutePath("");
        if (absolutePath.startsWith(root)) {
            String rel = absolutePath.substring(root.length());
            if (rel.startsWith(File.separator)) rel = rel.substring(1);
            return rel.replace("\\", "/");
        }
        return new File(absolutePath).getName();
    }
}
