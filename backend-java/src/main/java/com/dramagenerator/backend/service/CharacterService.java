package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.GenerateCharactersRequest;
import com.dramagenerator.backend.model.Character;
import com.dramagenerator.backend.model.ImageGeneration;
import java.util.List;

public interface CharacterService {
    String extractCharactersFromScript(Long episodeId);
    String extractCharacters(GenerateCharactersRequest request);
    void processCharacterExtractionGeneric(String taskId, Long dramaId, String content, Long episodeId);
    ImageGeneration generateCharacterImage(Long characterId, String model, String style);
    void batchGenerateCharacterImages(List<Long> characterIds, String model);
    List<Character> saveExtractedCharacters(Long dramaId, Long episodeId, List<java.util.Map<String, Object>> extractedChars) throws Exception;
    List<Character> listCharacters(Long dramaId);
    Character getCharacter(Long id);
    Character updateCharacter(Long id, Character character);
    void deleteCharacter(Long id);
}
