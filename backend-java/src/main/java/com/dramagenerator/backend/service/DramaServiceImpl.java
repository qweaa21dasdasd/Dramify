package com.dramagenerator.backend.service;

import com.dramagenerator.backend.dto.*;
import com.dramagenerator.backend.model.*;
import com.dramagenerator.backend.model.Character;
import com.dramagenerator.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DramaServiceImpl implements DramaService {

    private final DramaRepository dramaRepository;
    private final EpisodeRepository episodeRepository;
    private final CharacterRepository characterRepository;
    private final ImageGenerationRepository imageGenerationRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Drama createDrama(CreateDramaRequest request) {
        Drama drama = new Drama();
        drama.setTitle(request.getTitle());
        drama.setDescription(request.getDescription());
        drama.setGenre(request.getGenre());
        drama.setStyle(request.getStyle() != null ? request.getStyle() : "ghibli");
        drama.setTags(request.getTags());
        drama.setStatus("draft");
        
        return dramaRepository.save(drama);
    }

    @Override
    @Transactional(readOnly = true)
    public Drama getDrama(Long id) {
        Drama drama = dramaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drama not found"));
        
        // Calculate duration for each episode
        if (drama.getEpisodes() != null) {
            for (Episode episode : drama.getEpisodes()) {
                int totalDuration = 0;
                if (episode.getStoryboards() != null) {
                    for (Storyboard storyboard : episode.getStoryboards()) {
                        if (storyboard.getDuration() != null) {
                            totalDuration += storyboard.getDuration();
                        }
                    }
                }
                int durationMinutes = (totalDuration + 59) / 60;
                episode.setDuration(durationMinutes);
                
                // Check character image generation status
                if (episode.getCharacters() != null) {
                    for (Character character : episode.getCharacters()) {
                        updateCharacterImageStatus(character);
                    }
                }
                
                // Check scene image generation status
                if (episode.getScenes() != null) {
                    for (Scene scene : episode.getScenes()) {
                        updateSceneImageStatus(scene);
                    }
                }
            }
        }
        
        return drama;
    }

    private void updateCharacterImageStatus(Character character) {
        // Find latest image generation for character
        // This is a simplified version, in reality we might need a custom query
        // For now, assuming we don't fetch all image gens here to avoid N+1
        // Ideally this should be optimized
    }

    private void updateSceneImageStatus(Scene scene) {
        // Similar to character
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Drama> listDramas(DramaListQuery query) {
        Pageable pageable = PageRequest.of(query.getPage() - 1, query.getPageSize(), Sort.by("updatedAt").descending());
        
        Specification<Drama> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(query.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.getStatus()));
            }
            
            if (StringUtils.hasText(query.getGenre())) {
                predicates.add(criteriaBuilder.equal(root.get("genre"), query.getGenre()));
            }
            
            if (StringUtils.hasText(query.getKeyword())) {
                String likePattern = "%" + query.getKeyword() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("title"), likePattern),
                        criteriaBuilder.like(root.get("description"), likePattern)
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        return dramaRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public Drama updateDrama(Long id, UpdateDramaRequest request) {
        Drama drama = dramaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drama not found"));
        
        if (request.getTitle() != null) drama.setTitle(request.getTitle());
        if (request.getDescription() != null) drama.setDescription(request.getDescription());
        if (request.getGenre() != null) drama.setGenre(request.getGenre());
        if (request.getStyle() != null) drama.setStyle(request.getStyle());
        if (request.getTags() != null) drama.setTags(request.getTags());
        if (request.getStatus() != null) drama.setStatus(request.getStatus());
        
        return dramaRepository.save(drama);
    }

    @Override
    @Transactional
    public void deleteDrama(Long id) {
        if (!dramaRepository.existsById(id)) {
            throw new RuntimeException("Drama not found");
        }
        dramaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDramaStats() {
        long total = dramaRepository.count();
        // Group by status logic requires custom query or stream processing
        // Simplified for now
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        return stats;
    }

    @Override
    @Transactional
    public void saveOutline(Long id, SaveOutlineRequest request) {
        Drama drama = dramaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drama not found"));
        
        drama.setTitle(request.getTitle());
        drama.setDescription(request.getSummary());
        if (request.getGenre() != null) drama.setGenre(request.getGenre());
        
        if (request.getTags() != null) {
            try {
                drama.setTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize tags", e);
            }
        }
        
        dramaRepository.save(drama);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Character> getCharacters(Long id, Long episodeId) {
        if (!dramaRepository.existsById(id)) {
            throw new RuntimeException("Drama not found");
        }
        
        if (episodeId != null) {
            Episode episode = episodeRepository.findById(episodeId)
                    .orElseThrow(() -> new RuntimeException("Episode not found"));
            if (!episode.getDrama().getId().equals(id)) {
                throw new RuntimeException("Episode does not belong to this drama");
            }
            return episode.getCharacters();
        } else {
            return characterRepository.findByDramaId(id);
        }
    }

    @Override
    @Transactional
    public void saveCharacters(Long id, SaveCharactersRequest request) {
        Drama drama = dramaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drama not found"));
        
        // Find existing characters to reuse by name
        List<Character> existingCharacters = characterRepository.findByDramaId(id);
        Map<String, Character> existingCharMap = new HashMap<>();
        for (Character c : existingCharacters) {
            existingCharMap.put(c.getName(), c);
        }
        
        List<Character> targetCharacters = new ArrayList<>();
        
        for (Character charReq : request.getCharacters()) {
            Character character = null;
            
            // 1. Try to update by ID
            if (charReq.getId() != null) {
                Optional<Character> existingOpt = characterRepository.findById(charReq.getId());
                if (existingOpt.isPresent() && existingOpt.get().getDrama().getId().equals(id)) {
                    character = existingOpt.get();
                    // Update fields
                    character.setName(charReq.getName());
                    character.setRole(charReq.getRole());
                    character.setDescription(charReq.getDescription());
                    character.setPersonality(charReq.getPersonality());
                    character.setAppearance(charReq.getAppearance());
                    character.setImageUrl(charReq.getImageUrl());
                    character = characterRepository.save(character);
                }
            }
            
            // 2. Try to reuse by name if ID not provided or not found
            if (character == null && existingCharMap.containsKey(charReq.getName())) {
                character = existingCharMap.get(charReq.getName());
                // Optional: Update fields if reused? Go version doesn't update if reused by name, only logs it.
                // We'll stick to Go logic: just reuse.
                log.info("Reusing existing character: {} (ID: {})", character.getName(), character.getId());
            }
            
            // 3. Create new if not found
            if (character == null) {
                character = new Character();
                character.setDrama(drama);
                character.setName(charReq.getName());
                character.setRole(charReq.getRole());
                character.setDescription(charReq.getDescription());
                character.setPersonality(charReq.getPersonality());
                character.setAppearance(charReq.getAppearance());
                character.setImageUrl(charReq.getImageUrl());
                character = characterRepository.save(character);
                log.info("Created new character: {} (ID: {})", character.getName(), character.getId());
            }
            
            targetCharacters.add(character);
        }
        
        if (request.getEpisodeId() != null) {
            Episode episode = episodeRepository.findById(request.getEpisodeId())
                    .orElseThrow(() -> new RuntimeException("Episode not found"));
            
            if (!episode.getDrama().getId().equals(id)) {
                throw new RuntimeException("Episode does not belong to this drama");
            }
            
            // Add characters to episode if not already present
            List<Character> episodeCharacters = episode.getCharacters();
            if (episodeCharacters == null) {
                episodeCharacters = new ArrayList<>();
                episode.setCharacters(episodeCharacters);
            }
            
            for (Character targetChar : targetCharacters) {
                if (!episodeCharacters.contains(targetChar)) {
                    episodeCharacters.add(targetChar);
                }
            }
            episodeRepository.save(episode);
        }
    }

    @Override
    @Transactional
    public void saveEpisodes(Long id, SaveEpisodesRequest request) {
        Drama drama = dramaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drama not found"));
        
        // Delete all existing episodes for this drama
        episodeRepository.deleteByDramaId(id);
        
        // Create new episodes
        for (SaveEpisodesRequest.EpisodeDTO epReq : request.getEpisodes()) {
            Episode episode = new Episode();
            episode.setDrama(drama);
            episode.setEpisodeNumber(epReq.getEpisodeNumber());
            episode.setTitle(epReq.getTitle() != null ? epReq.getTitle() : "第" + epReq.getEpisodeNumber() + "集");
            episode.setDescription(epReq.getDescription());
            episode.setScriptContent(epReq.getScriptContent());
            episode.setDuration(epReq.getDuration() != null ? epReq.getDuration() : 0);
            episode.setStatus(epReq.getStatus() != null ? epReq.getStatus() : "draft");
            
            episodeRepository.save(episode);
        }
    }

    @Override
    @Transactional
    public void saveProgress(Long id, SaveProgressRequest request) {
        Drama drama = dramaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drama not found"));
        
        try {
            Map<String, Object> metadataMap = new HashMap<>();
            if (drama.getMetadata() != null) {
                metadataMap = objectMapper.readValue(drama.getMetadata(), Map.class);
            }
            
            metadataMap.put("current_step", request.getCurrentStep());
            if (request.getStepData() != null) {
                metadataMap.put("step_data", request.getStepData());
            }
            
            drama.setMetadata(objectMapper.writeValueAsString(metadataMap));
            dramaRepository.save(drama);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process metadata", e);
        }
    }
}
