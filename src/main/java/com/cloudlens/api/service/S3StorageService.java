package com.cloudlens.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.storage.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(InputStream inputStream, String originalFileName, long fileSize) {
        String internalFileName = UUID.randomUUID().toString();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(internalFileName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, fileSize));

            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, internalFileName);
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to cloud storage", e);
        }
    }

    @Override
    public void deleteFile(String storageUrl) {
        try {
            URI uri = new URI(storageUrl);
            String key = uri.getPath().substring(1);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted file from S3: {}", key);
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file from cloud storage", e);
        }
    }

    @Override
    public String generatePresignedUrl(String storageUrl) {
        try {
            URI uri = new URI(storageUrl);
            String key = uri.getPath().substring(1);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage());
            return storageUrl;
        }
    }
}
