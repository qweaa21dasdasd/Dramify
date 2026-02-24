package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    List<Episode> findByDramaId(Long dramaId);
}
