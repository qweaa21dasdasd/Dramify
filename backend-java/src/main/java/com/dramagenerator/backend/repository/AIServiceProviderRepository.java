package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.AIServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIServiceProviderRepository extends JpaRepository<AIServiceProvider, Long> {
}
