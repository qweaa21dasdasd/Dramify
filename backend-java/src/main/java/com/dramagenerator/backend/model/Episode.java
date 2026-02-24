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
@Table(name = "episodes")
@Where(clause = "deleted_at IS NULL")
public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drama_id", nullable = false)
    private Drama drama;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(name = "script_content", columnDefinition = "LONGTEXT")
    private String scriptContent;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration")
    private Integer duration = 0;

    @Column(length = 20)
    private String status = "draft";

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(length = 500)
    private String thumbnail;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<Storyboard> storyboards;

    @ManyToMany(mappedBy = "episodes")
    private List<Character> characters;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<Scene> scenes;
}
