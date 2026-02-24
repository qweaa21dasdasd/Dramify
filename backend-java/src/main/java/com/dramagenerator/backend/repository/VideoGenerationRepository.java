package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.VideoGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoGenerationRepository extends JpaRepository<VideoGeneration, Long> {
    List<VideoGeneration> findByDramaId(Long dramaId);
    List<VideoGeneration> findByStoryboardId(Long storyboardId);
    List<VideoGeneration> findByTaskId(String taskId);
}
