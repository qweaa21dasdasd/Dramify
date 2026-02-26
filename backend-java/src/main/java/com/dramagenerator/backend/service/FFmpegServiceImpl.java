package com.dramagenerator.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FFmpegServiceImpl implements FFmpegService {

    @Override
    public String mergeVideos(List<String> videoPaths, String outputDir) throws IOException, InterruptedException {
        // Create output directory if not exists
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Create file list for concat
        File listFile = File.createTempFile("ffmpeg_list_", ".txt");
        List<String> lines = videoPaths.stream()
                .map(path -> "file '" + new File(path).getAbsolutePath() + "'")
                .collect(Collectors.toList());
        Files.write(listFile.toPath(), lines);

        String outputFilename = "merged_" + UUID.randomUUID() + ".mp4";
        File outputFile = new File(dir, outputFilename);

        // Build ffmpeg command
        // Re-encoding to ensure compatibility
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-f", "concat",
                "-safe", "0",
                "-i", listFile.getAbsolutePath(),
                "-c:v", "libx264",
                "-preset", "fast", // Fast encoding
                "-crf", "23",      // Standard quality
                "-c:a", "aac",
                "-y",              // Overwrite
                outputFile.getAbsolutePath()
        );
        
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        // Read output to avoid blocking
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // log.debug(line); // Verbose logging
            }
        }

        if (!process.waitFor(10, TimeUnit.MINUTES)) {
            process.destroy();
            throw new IOException("FFmpeg process timed out");
        }

        if (process.exitValue() != 0) {
            throw new IOException("FFmpeg process failed with exit code " + process.exitValue());
        }
        
        // Cleanup list file
        listFile.delete();

        return outputFile.getAbsolutePath();
    }

    @Override
    public String extractAudio(String videoPath, String outputDir) throws IOException, InterruptedException {
        // Create output directory if not exists
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String outputFilename = "audio_" + UUID.randomUUID() + ".mp3";
        File outputFile = new File(dir, outputFilename);

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", videoPath,
                "-vn",             // No video
                "-acodec", "libmp3lame",
                "-q:a", "2",       // High quality
                "-y",              // Overwrite
                outputFile.getAbsolutePath()
        );
        
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // log.debug(line);
            }
        }

        if (!process.waitFor(5, TimeUnit.MINUTES)) {
            process.destroy();
            throw new IOException("FFmpeg audio extraction timed out");
        }

        if (process.exitValue() != 0) {
            throw new IOException("FFmpeg process failed with exit code " + process.exitValue());
        }

        return outputFile.getAbsolutePath();
    }

    @Override
    public double getVideoDuration(String videoPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    videoPath
            );
            
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    return Double.parseDouble(line.trim());
                }
            }
            process.waitFor();
        } catch (Exception e) {
            log.error("Failed to get video duration for {}", videoPath, e);
        }
        return 0;
    }
}
