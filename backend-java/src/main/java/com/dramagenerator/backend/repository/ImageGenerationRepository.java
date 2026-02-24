package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.ImageGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageGenerationRepository extends JpaRepository<ImageGeneration, Long> {
    List<ImageGeneration> findByDramaId(Long dramaId);
    List<ImageGeneration> findByStoryboardId(Long storyboardId);
    List<ImageGeneration> findByTaskId(String taskId);
}
