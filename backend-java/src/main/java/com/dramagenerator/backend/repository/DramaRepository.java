package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.Drama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DramaRepository extends JpaRepository<Drama, Long>, JpaSpecificationExecutor<Drama> {
}
