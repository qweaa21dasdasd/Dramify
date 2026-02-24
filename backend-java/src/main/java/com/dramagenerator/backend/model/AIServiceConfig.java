package com.dramagenerator.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ai_service_configs")
public class AIServiceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_type", length = 50, nullable = false)
    private String serviceType; // text, image, video

    @Column(length = 50)
    private String provider; // openai, gemini, etc.

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "base_url", length = 255, nullable = false)
    private String baseUrl;

    @Column(name = "api_key", length = 255, nullable = false)
    private String apiKey;

    @Column(columnDefinition = "TEXT")
    private String model; // JSON string array or string

    @Column(length = 255)
    private String endpoint;

    @Column(name = "query_endpoint", length = 255)
    private String queryEndpoint;

    private Integer priority = 0;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String settings;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
