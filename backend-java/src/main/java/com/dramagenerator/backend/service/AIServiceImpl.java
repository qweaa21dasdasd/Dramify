package com.dramagenerator.backend.service;

import com.dramagenerator.backend.model.AIServiceConfig;
import com.dramagenerator.backend.repository.AIServiceConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    private final AIServiceConfigRepository configRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Override
    public String generateText(String prompt, String systemPrompt, Map<String, Object> options) {
        // 1. Find default text generation config
        AIServiceConfig config = findActiveConfig("text");
        if (config == null) {
            // Fallback or error
            throw new RuntimeException("No active AI service configuration found for type 'text'");
        }

        // 2. Prepare Request
        String url = config.getBaseUrl();
        if (url == null) url = "https://api.openai.com/v1"; // Default fallback
        if (!url.endsWith("/")) url += "/";
        if (!url.contains("chat/completions")) {
             url += "chat/completions";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        Map<String, Object> body = new HashMap<>();
        
        // Model selection
        String model = (String) options.get("model");
        if (model == null) {
            // Parse from config model field (JSON array or string)
            model = parseDefaultModel(config.getModel());
        }
        body.put("model", model);

        // Messages
        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", prompt));
        body.put("messages", messages);

        // Other options
        // Default max_tokens to prevent truncation if not provided
        if (options.containsKey("max_tokens")) {
             body.put("max_tokens", options.get("max_tokens"));
        } else {
             body.put("max_tokens", 4000); // Reasonable default for text generation
        }
        
        if (options.containsKey("temperature")) body.put("temperature", options.get("temperature"));

        // Special handling for gemini-like models that might need different structure? 
        // Assuming OpenAI compatible API for now based on config.
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            log.info("Calling AI Service: {} with model {}", url, model);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                return root.get("choices").get(0).get("message").get("content").asText();
            } else {
                throw new RuntimeException("Invalid AI response format: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("AI Service call failed", e);
            // Include response body in error if possible
            throw new RuntimeException("AI Service call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateImage(String prompt, Map<String, Object> options) {
        // 1. Find default image generation config
        AIServiceConfig config = findActiveConfig("image");
        if (config == null) {
            throw new RuntimeException("No active AI service configuration found for type 'image'");
        }

        // 2. Prepare Request
        String url = config.getBaseUrl();
        if (url == null) url = "https://api.openai.com/v1"; // Default fallback
        if (!url.endsWith("/")) url += "/";
        if (!url.contains("images/generations")) {
             url += "images/generations";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("prompt", prompt);
        
        // Model selection
        String model = (String) options.get("model");
        if (model == null) {
            model = parseDefaultModel(config.getModel());
        }
        body.put("model", model);
        body.put("n", 1); // Default to 1 image

        if (options.containsKey("size")) body.put("size", options.get("size"));
        if (options.containsKey("quality")) body.put("quality", options.get("quality"));
        if (options.containsKey("style")) body.put("style", options.get("style"));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            log.info("Calling AI Service for Image: {} with model {}", url, model);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("data") && root.get("data").isArray() && root.get("data").size() > 0) {
                return root.get("data").get(0).get("url").asText();
            } else {
                throw new RuntimeException("Invalid AI response format: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("AI Service call failed", e);
            throw new RuntimeException("AI Service call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateVideo(String prompt, Map<String, Object> options) {
        // 1. Find default video generation config
        AIServiceConfig config = findActiveConfig("video");
        if (config == null) {
            // Fallback or use image config if video not found? No, better to fail.
            // Or use a dummy implementation for testing if no config.
             throw new RuntimeException("No active AI service configuration found for type 'video'");
        }

        // 2. Prepare Request
        String url = config.getBaseUrl();
        if (url == null) url = "https://api.openai.com/v1"; 
        if (!url.endsWith("/")) url += "/";
        // Assume OpenAI Sora-like or similar endpoint
        if (!url.contains("videos/generations")) {
             url += "videos/generations"; // Hypothetical endpoint
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("prompt", prompt);
        
        String model = (String) options.get("model");
        if (model == null) {
            model = parseDefaultModel(config.getModel());
        }
        body.put("model", model);
        
        // Add other options...
        if (options.containsKey("image_url")) body.put("image_url", options.get("image_url"));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            log.info("Calling AI Service for Video: {} with model {}", url, model);
            // This is a blocking call. If the API is async, this might return a task ID.
            // For now, assuming sync or blocking wrapper.
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            // Adjust parsing based on actual API
            if (root.has("data") && root.get("data").isArray() && root.get("data").size() > 0) {
                return root.get("data").get(0).get("url").asText();
            } else if (root.has("id")) {
                // Task ID returned
                return "TASK:" + root.get("id").asText();
            } else {
                throw new RuntimeException("Invalid AI response format: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("AI Service call failed", e);
            throw new RuntimeException("AI Service call failed: " + e.getMessage(), e);
        }
    }

    private AIServiceConfig findActiveConfig(String type) {
        // Simple logic: find first active default, or just first active
        List<AIServiceConfig> configs = configRepository.findAll();
        // If config is empty, create a default one for test if not exists
        if (configs.isEmpty()) {
            return null; // Or create a dummy one? Better to fail if no config.
        }
        
        return configs.stream()
                .filter(c -> type.equalsIgnoreCase(c.getServiceType()) && Boolean.TRUE.equals(c.getIsActive()))
                .sorted((c1, c2) -> {
                    // Default first, then priority desc
                    boolean d1 = Boolean.TRUE.equals(c1.getIsDefault());
                    boolean d2 = Boolean.TRUE.equals(c2.getIsDefault());
                    if (d1 && !d2) return -1;
                    if (!d1 && d2) return 1;
                    return Integer.compare(c2.getPriority() != null ? c2.getPriority() : 0, 
                                           c1.getPriority() != null ? c1.getPriority() : 0);
                })
                .findFirst()
                .orElse(null);
    }

    private String parseDefaultModel(String modelField) {
        if (modelField == null || modelField.isEmpty()) return "gpt-3.5-turbo";
        try {
            // Try parsing as JSON array
            if (modelField.trim().startsWith("[")) {
                List<String> models = objectMapper.readValue(modelField, List.class);
                if (!models.isEmpty()) return models.get(0);
            }
        } catch (Exception e) {
            // ignore
        }
        return modelField;
    }
}
