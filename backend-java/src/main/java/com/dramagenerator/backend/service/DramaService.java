package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.*;
import com.dramagenerator.backend.model.Drama;
import com.dramagenerator.backend.model.Character;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DramaService {
    Drama createDrama(CreateDramaRequest request);
    Drama getDrama(Long id);
    Page<Drama> listDramas(DramaListQuery query);
    Drama updateDrama(Long id, UpdateDramaRequest request);
    void deleteDrama(Long id);
    Map<String, Object> getDramaStats();
    void saveOutline(Long id, SaveOutlineRequest request);
    List<Character> getCharacters(Long id, Long episodeId);
    void saveCharacters(Long id, SaveCharactersRequest request);
    void saveEpisodes(Long id, SaveEpisodesRequest request);
    void saveProgress(Long id, SaveProgressRequest request);
}
