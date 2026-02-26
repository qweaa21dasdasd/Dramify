package com.dramagenerator.backend.service;

import com.dramagenerator.backend.model.AsyncTask;
import com.dramagenerator.backend.repository.TaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AsyncTask createTask(String type, String resourceId) {
        AsyncTask task = new AsyncTask();
        task.setType(type);
        task.setResourceId(resourceId);
        task.setStatus("pending");
        // Remove manual ID setting, let UUID generator handle it? 
        // Or if we need string ID, make sure entity uses UUID generator correctly.
        // JPA with @GeneratedValue(strategy = GenerationType.UUID) should work if provider supports it.
        // Hibernate 6 supports UUID generation.
        task.setCreatedAt(LocalDateTime.now());
        task.setProgress(0);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void updateTaskStatus(String taskId, String status, Integer progress, String message) {
        AsyncTask task = taskRepository.findById(taskId)
                .orElse(null); // Changed to orElse(null) to handle async race condition gracefully?
                
        if (task == null) {
            log.warn("Task not found for update: " + taskId);
            return;
        }
        
        task.setStatus(status);
        if (progress != null) task.setProgress(progress);
        if (message != null) task.setMessage(message);
        
        if ("completed".equals(status) || "failed".equals(status)) {
            task.setCompletedAt(LocalDateTime.now());
        }
        
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void updateTaskResult(String taskId, Map<String, Object> result) {
        AsyncTask task = taskRepository.findById(taskId)
                .orElse(null);
        
        if (task == null) {
            log.warn("Task not found for result update: " + taskId);
            return;
        }
        
        try {
            task.setResult(objectMapper.writeValueAsString(result));
            task.setStatus("completed");
            task.setProgress(100);
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize task result", e);
            updateTaskError(taskId, "Failed to serialize result");
        }
    }

    @Override
    @Transactional
    public void updateTaskError(String taskId, String error) {
        AsyncTask task = taskRepository.findById(taskId)
                .orElse(null);
                
        if (task == null) {
            log.warn("Task not found for error update: " + taskId);
            return;
        }
        
        task.setError(error);
        task.setStatus("failed");
        task.setCompletedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public AsyncTask getTask(String taskId) {
        return taskRepository.findById(taskId)
                .orElse(null);
    }
}
