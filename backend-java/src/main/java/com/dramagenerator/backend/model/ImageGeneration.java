package com.dramagenerator.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "image_generations")
public class ImageGeneration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drama_id", nullable = false)
    private Drama drama;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storyboard_id")
    private Storyboard storyboard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id")
    private Scene scene;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prop_id")
    private Prop prop;

    @Column(name = "image_type", length = 20)
    private String imageType = "storyboard";

    @Column(name = "frame_type", length = 20)
    private String frameType;

    @Column(length = 50, nullable = false)
    private String provider;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(name = "negative_prompt", columnDefinition = "TEXT")
    private String negativePrompt;

    @Column(length = 100)
    private String model;

    @Column(length = 20)
    private String size;

    @Column(length = 20)
    private String quality;

    @Column(length = 50)
    private String style;

    private Integer steps;

    @Column(name = "cfg_scale")
    private Double cfgScale;

    private Long seed;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "minio_url", columnDefinition = "TEXT")
    private String minioUrl;

    @Column(name = "local_path", columnDefinition = "TEXT")
    private String localPath;

    @Column(length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "task_id", length = 200)
    private String taskId;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    private Integer width;

    private Integer height;

    @Column(name = "reference_images", columnDefinition = "TEXT")
    private String referenceImages;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
