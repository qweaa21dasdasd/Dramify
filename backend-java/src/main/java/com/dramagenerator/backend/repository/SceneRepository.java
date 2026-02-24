package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findByDramaId(Long dramaId);
    List<Scene> findByEpisodeId(Long episodeId);
}
