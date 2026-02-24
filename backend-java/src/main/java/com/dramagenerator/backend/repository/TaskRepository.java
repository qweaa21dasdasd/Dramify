package com.dramagenerator.backend.repository;

import com.dramagenerator.backend.model.AsyncTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<AsyncTask, String> {
}
