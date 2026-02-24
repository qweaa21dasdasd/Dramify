package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.Timeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelineRepository extends JpaRepository<Timeline, Long> {
    List<Timeline> findByDramaId(Long dramaId);
    List<Timeline> findByEpisodeId(Long episodeId);
}
