package com.dramagenerator.backend.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    String store(MultipartFile file, String subDir);
    String storeFromUrl(String url, String subDir);
    String getAbsolutePath(String relativePath);
}
