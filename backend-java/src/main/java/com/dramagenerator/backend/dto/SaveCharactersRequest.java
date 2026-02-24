package com.dramagenerator.backend.dto;

import com.dramagenerator.backend.model.Character;
import lombok.Data;
import java.util.List;

@Data
public class SaveCharactersRequest {
    private List<Character> characters;
    private Long episodeId;
}
