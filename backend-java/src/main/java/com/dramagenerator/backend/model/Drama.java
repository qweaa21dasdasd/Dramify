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
@Table(name = "dramas")
@Where(clause = "deleted_at IS NULL")
public class Drama {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String genre;

    @Column(length = 50)
    private String style = "realistic";

    @Column(name = "total_episodes")
    private Integer totalEpisodes = 1;

    @Column(name = "total_duration")
    private Integer totalDuration = 0;

    @Column(length = 20, nullable = false)
    private String status = "draft";

    @Column(length = 500)
    private String thumbnail;

    @Column(columnDefinition = "TEXT") // JSON stored as TEXT
    private String tags;

    @Column(columnDefinition = "TEXT") // JSON stored as TEXT
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "drama", cascade = CascadeType.ALL)
    private List<Episode> episodes;

    @OneToMany(mappedBy = "drama", cascade = CascadeType.ALL)
    private List<Character> characters;

    @OneToMany(mappedBy = "drama", cascade = CascadeType.ALL)
    private List<Scene> scenes;

    @OneToMany(mappedBy = "drama", cascade = CascadeType.ALL)
    private List<Prop> props;
}
