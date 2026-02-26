package com.dramagenerator.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "async_tasks")
@Where(clause = "deleted_at IS NULL")
public class AsyncTask {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(length = 50, nullable = false)
    private String type; // storyboard_generation, etc.

    @Column(length = 20, nullable = false)
    private String status; // pending, processing, completed, failed

    private Integer progress = 0;

    @Column(length = 500)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String error;

    @Column(columnDefinition = "TEXT")
    private String result; // JSON stored as TEXT

    @Column(name = "resource_id", length = 36)
    private String resourceId;

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
