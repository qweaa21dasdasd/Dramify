package com.dramagenerator.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateStoryboardRequest {
    @com.fasterxml.jackson.annotation.JsonProperty("episode_id")
    private Long episodeId;
    
    @com.fasterxml.jackson.annotation.JsonProperty("scene_id")
    private Long sceneId;
    
    @com.fasterxml.jackson.annotation.JsonProperty("storyboard_number")
    private Integer storyboardNumber;
    
    private String title;
    private String location;
    private String time;
    
    @com.fasterxml.jackson.annotation.JsonProperty("shot_type")
    private String shotType;
    
    private String angle;
    private String movement;
    private String description;
    private String action;
    private String result;
    private String atmosphere;
    private String dialogue;
    
    @com.fasterxml.jackson.annotation.JsonProperty("bgm_prompt")
    private String bgmPrompt;
    
    @com.fasterxml.jackson.annotation.JsonProperty("sound_effect")
    private String soundEffect;
    
    private Integer duration;
    private List<Long> characters;
}
