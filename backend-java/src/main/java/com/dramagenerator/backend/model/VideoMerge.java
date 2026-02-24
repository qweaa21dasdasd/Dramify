package com.dramagenerator.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "video_merges")
@Where(clause = "deleted_at IS NULL")
public class VideoMerge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drama_id", nullable = false)
    private Drama drama;

    @Column(length = 200)
    private String title;

    @Column(length = 50, nullable = false)
    private String provider;

    @Column(length = 100)
    private String model;

    @Column(length = 20, nullable = false)
    private String status = "pending";

    @Column(columnDefinition = "TEXT", nullable = false)
    private String scenes; // JSON stored as TEXT

    @Column(name = "merged_url", length = 500)
    private String mergedUrl;

    private Integer duration;

    @Column(name = "task_id", length = 100)
    private String taskId;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
