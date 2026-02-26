package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.AudioExtractionRequest;
import java.util.Map;

public interface AudioExtractionService {
    Map<String, Object> extractAudio(AudioExtractionRequest request);
}
