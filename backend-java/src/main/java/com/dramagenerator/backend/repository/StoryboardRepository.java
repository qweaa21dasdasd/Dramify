package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.Storyboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryboardRepository extends JpaRepository<Storyboard, Long> {
    List<Storyboard> findByEpisodeId(Long episodeId);
    List<Storyboard> findBySceneId(Long sceneId);
}
