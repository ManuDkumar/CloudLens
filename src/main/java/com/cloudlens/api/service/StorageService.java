package com.cloudlens.api.service;

import java.io.InputStream;

public interface StorageService {
    String uploadFile(InputStream inputStream, String originalFileName, long fileSize);
    String generatePresignedUrl(String storageUrl);
    void deleteFile(String storageUrl);
}
