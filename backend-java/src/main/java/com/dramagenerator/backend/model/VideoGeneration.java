package com.dramagenerator.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "video_generations")
@Where(clause = "deleted_at IS NULL")
public class VideoGeneration {
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
    @JoinColumn(name = "image_gen_id")
    private ImageGeneration imageGen;

    @Column(length = 50, nullable = false)
    private String provider;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(length = 100)
    private String model;

    @Column(name = "reference_mode", length = 20)
    private String referenceMode;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "first_frame_url", length = 1000)
    private String firstFrameUrl;

    @Column(name = "last_frame_url", length = 1000)
    private String lastFrameUrl;

    @Column(name = "reference_image_urls", columnDefinition = "TEXT")
    private String referenceImageUrls;

    private Integer duration;

    private Integer fps;

    @Column(length = 50)
    private String resolution;

    @Column(name = "aspect_ratio", length = 20)
    private String aspectRatio;

    @Column(length = 100)
    private String style;

    @Column(name = "motion_level")
    private Integer motionLevel;

    @Column(name = "camera_motion", length = 100)
    private String cameraMotion;

    private Long seed;

    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Column(name = "minio_url", length = 1000)
    private String minioUrl;

    @Column(name = "local_path", length = 500)
    private String localPath;

    @Column(length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "task_id", length = 200)
    private String taskId;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    private Integer width;

    private Integer height;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
