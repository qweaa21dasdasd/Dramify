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
@Table(name = "characters")
@Where(clause = "deleted_at IS NULL")
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drama_id", nullable = false)
    private Drama drama;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 50)
    private String role;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String appearance;

    @Column(columnDefinition = "TEXT")
    private String personality;

    @Column(name = "voice_style", length = 200)
    private String voiceStyle;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "local_path", columnDefinition = "TEXT")
    private String localPath;

    @Column(name = "reference_images", columnDefinition = "TEXT")
    private String referenceImages;

    @Column(name = "seed_value", length = 100)
    private String seedValue;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

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
        name = "episode_characters",
        joinColumns = @JoinColumn(name = "character_id"),
        inverseJoinColumns = @JoinColumn(name = "episode_id")
    )
    private List<Episode> episodes;

    @Transient
    private String imageGenerationStatus;

    @Transient
    private String imageGenerationError;
}
