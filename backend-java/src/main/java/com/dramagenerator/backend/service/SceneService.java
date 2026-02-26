package com.dramagenerator.backend.service;

import com.dramagenerator.backend.model.Scene;
import java.util.List;

public interface SceneService {
    String extractBackgroundsFromScript(Long episodeId, String model, String style);
    void processBackgroundExtraction(String taskId, Long episodeId, String model, String style);
    void batchGenerateScenes(Long episodeId);
    void generateSceneImage(Long sceneId, String model);
    List<Scene> listScenes(Long episodeId);
    Scene getScene(Long id);
    Scene updateScene(Long id, Scene scene);
    void deleteScene(Long id);
}
