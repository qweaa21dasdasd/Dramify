package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.AIServiceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIServiceConfigRepository extends JpaRepository<AIServiceConfig, Long> {
    List<AIServiceConfig> findByServiceType(String serviceType);
    Optional<AIServiceConfig> findByServiceTypeAndIsDefaultTrue(String serviceType);
}
