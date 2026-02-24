package com.dramagenerator.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "storyboards")
@Where(clause = "deleted_at IS NULL")
public class Storyboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id")
    private Scene scene;

    @Column(name = "storyboard_number", nullable = false)
    private Integer storyboardNumber;

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String location;

    @Column(length = 255)
    private String time;

    @Column(name = "shot_type", length = 100)
    private String shotType;

    @Column(length = 100)
    private String angle;

    @Column(length = 100)
    private String movement;

    @Column(columnDefinition = "TEXT")
    private String action;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(columnDefinition = "TEXT")
    private String atmosphere;

    @Column(name = "image_prompt", columnDefinition = "TEXT")
    private String imagePrompt;

    @Column(name = "video_prompt", columnDefinition = "TEXT")
    private String videoPrompt;

    @Column(name = "bgm_prompt", columnDefinition = "TEXT")
    private String bgmPrompt;

    @Column(name = "sound_effect", length = 255)
    private String soundEffect;

    @Column(columnDefinition = "TEXT")
    private String dialogue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration")
    private Integer duration = 5;

    @Column(name = "composed_image", columnDefinition = "TEXT")
    private String composedImage;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(length = 20)
    private String status = "pending";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToMany
    @JoinTable(
        name = "storyboard_characters",
        joinColumns = @JoinColumn(name = "storyboard_id"),
        inverseJoinColumns = @JoinColumn(name = "character_id")
    )
    private List<Character> characters;

    @ManyToMany
    @JoinTable(
        name = "storyboard_props",
        joinColumns = @JoinColumn(name = "storyboard_id"),
        inverseJoinColumns = @JoinColumn(name = "prop_id")
    )
    private List<Prop> props;
}
