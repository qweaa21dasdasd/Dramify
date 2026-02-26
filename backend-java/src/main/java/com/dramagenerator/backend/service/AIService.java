package com.dramagenerator.backend.service;

import java.util.Map;

public interface AIService {
    String generateText(String prompt, String systemPrompt, Map<String, Object> options);
    String generateImage(String prompt, Map<String, Object> options);
    String generateVideo(String prompt, Map<String, Object> options);
}
