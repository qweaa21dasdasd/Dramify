package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.Prop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropRepository extends JpaRepository<Prop, Long> {
    List<Prop> findByDramaId(Long dramaId);
}
