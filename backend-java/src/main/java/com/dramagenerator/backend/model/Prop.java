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
@Table(name = "props")
@Where(clause = "deleted_at IS NULL")
public class Prop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drama_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Drama drama;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 50)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "local_path", columnDefinition = "TEXT")
    private String localPath;

    @Column(name = "reference_images", columnDefinition = "TEXT")
    private String referenceImages;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToMany(mappedBy = "props")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Storyboard> storyboards;
}
