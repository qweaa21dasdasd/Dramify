package com.dramagenerator.backend.controller;

import com.dramagenerator.backend.model.AsyncTask;
import com.dramagenerator.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/{id}")
    public ResponseEntity<com.dramagenerator.backend.common.ApiResponse<AsyncTask>> getTask(@PathVariable String id) {
        AsyncTask task = taskService.getTask(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(com.dramagenerator.backend.common.ApiResponse.success(task));
    }
}
