package com.dramagenerator.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scenes")
@Where(clause = "deleted_at IS NULL")
public class Scene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drama_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Drama drama;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Episode episode;

    @Column(length = 200, nullable = false)
    private String location;

    @Column(length = 100, nullable = false)
    private String time;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(name = "storyboard_count")
    private Integer storyboardCount = 1;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "local_path", columnDefinition = "TEXT")
    private String localPath;

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

    @Transient
    private String imageGenerationStatus;

    @Transient
    private String imageGenerationError;
}
