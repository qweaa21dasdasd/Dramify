package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByDramaId(Long dramaId);
    List<Asset> findByEpisodeId(Long episodeId);
    List<Asset> findByStoryboardId(Long storyboardId);
}
