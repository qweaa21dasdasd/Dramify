package com.dramagenerator.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "clip_effects")
@Where(clause = "deleted_at IS NULL")
public class ClipEffect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clip_id", nullable = false)
    private TimelineClip clip;

    @Column(length = 50, nullable = false)
    private String type; // filter, color, etc.

    @Column(length = 100)
    private String name;

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    @Column(name = "`order`")
    private Integer order = 0;

    @Column(columnDefinition = "TEXT")
    private String config; // JSON stored as TEXT

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
