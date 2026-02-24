package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findByDramaId(Long dramaId);
}
