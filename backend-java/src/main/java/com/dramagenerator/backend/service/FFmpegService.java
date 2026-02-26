package com.dramagenerator.backend.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FFmpegService {
    String mergeVideos(List<String> videoPaths, String outputDir) throws IOException, InterruptedException;
    String extractAudio(String videoPath, String outputDir) throws IOException, InterruptedException;
    double getVideoDuration(String videoPath);
}
