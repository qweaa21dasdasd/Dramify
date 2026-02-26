package com.dramagenerator.backend.common;

import org.springframework.stereotype.Component;

@Component
public class PromptI18n {
    public String getStoryboardSystemPrompt() {
        return "You are a professional storyboard artist and director. Your task is to break down the script into detailed storyboards.";
    }

    public String formatUserPrompt(String key) {
        // Simplified for now
        return "";
    }
}
