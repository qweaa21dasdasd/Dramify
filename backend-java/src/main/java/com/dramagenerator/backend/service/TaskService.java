package com.dramagenerator.backend.service;

import com.dramagenerator.backend.model.AsyncTask;
import java.util.Map;

public interface TaskService {
    void updateTaskStatus(String taskId, String status, Integer progress, String message);
    void updateTaskResult(String taskId, Map<String, Object> result);
    void updateTaskError(String taskId, String error);
    AsyncTask getTask(String taskId);
    AsyncTask createTask(String type, String resourceId);
}
