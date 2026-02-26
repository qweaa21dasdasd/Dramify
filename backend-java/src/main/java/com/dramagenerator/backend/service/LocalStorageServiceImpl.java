package com.dramagenerator.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class LocalStorageServiceImpl implements StorageService {

    private final Path rootLocation;

    public LocalStorageServiceImpl(@Value("${app.storage.local-path:./data/storage}") String localPath) {
        this.rootLocation = Paths.get(localPath);
        init();
    }

    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDir) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new RuntimeException(
                        "Cannot store file with relative path outside current directory "
                                + filename);
            }
            
            Path subPath = rootLocation.resolve(subDir);
            Files.createDirectories(subPath);
            
            String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, subPath.resolve(uniqueFilename),
                    StandardCopyOption.REPLACE_EXISTING);
            }
            return subDir + "/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }

    @Override
    public String storeFromUrl(String urlString, String subDir) {
        try {
            URL url = new URL(urlString);
            String filename = Paths.get(url.getPath()).getFileName().toString();
            if (filename.isEmpty()) {
                filename = "image_" + System.currentTimeMillis() + ".jpg"; // Default
            }
            
            Path subPath = rootLocation.resolve(subDir);
            Files.createDirectories(subPath);
            
            String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
            try (InputStream inputStream = url.openStream()) {
                Files.copy(inputStream, subPath.resolve(uniqueFilename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return subDir + "/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file from URL " + urlString, e);
        }
    }

    @Override
    public String getAbsolutePath(String relativePath) {
        return rootLocation.resolve(relativePath).toString();
    }
}
