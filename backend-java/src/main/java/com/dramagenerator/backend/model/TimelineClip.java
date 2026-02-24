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
@Table(name = "timeline_clips")
@Where(clause = "deleted_at IS NULL")
public class TimelineClip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private TimelineTrack track;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storyboard_id")
    private Storyboard storyboard;

    @Column(length = 200)
    private String name;

    @Column(name = "start_time", nullable = false)
    private Integer startTime;

    @Column(name = "end_time", nullable = false)
    private Integer endTime;

    @Column(nullable = false)
    private Integer duration;

    @Column(name = "trim_start")
    private Integer trimStart;

    @Column(name = "trim_end")
    private Integer trimEnd;

    private Double speed = 1.0;

    private Integer volume;

    @Column(name = "is_muted")
    private Boolean isMuted = false;

    @Column(name = "fade_in")
    private Integer fadeIn;

    @Column(name = "fade_out")
    private Integer fadeOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_in_id")
    private ClipTransition transitionIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_out_id")
    private ClipTransition transitionOut;

    @OneToMany(mappedBy = "clip", cascade = CascadeType.ALL)
    private List<ClipEffect> effects;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
