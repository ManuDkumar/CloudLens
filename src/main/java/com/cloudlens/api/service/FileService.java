package com.cloudlens.api.service;

import com.cloudlens.api.dto.FileResponse;
import com.cloudlens.api.entity.FileMetadata;
import com.cloudlens.api.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final StorageService storageService;
    private final FileMetadataRepository repository;
    private final MetadataService metadataService;

    public Page<FileResponse> getAllFiles(int page, int size, String search, String currentUser, boolean isAdmin) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadTimestamp"));
        Page<FileMetadata> metadataPage;
        if (search != null && !search.isBlank()) {
            if (isAdmin) {
                metadataPage = repository.searchFiles(search.trim(), pageable);
            } else {
                metadataPage = repository.searchUserFiles(currentUser, search.trim(), pageable);
            }
        } else {
            if (isAdmin) {
                metadataPage = repository.findAllByOrderByUploadTimestampDesc(pageable);
            } else {
                metadataPage = repository.findByUploadedByOrderByUploadTimestampDesc(currentUser, pageable);
            }
        }
        return metadataPage.map(this::mapToResponse);
    }

    public FileResponse uploadFile(MultipartFile file, String description, String uploadedBy) throws IOException {
        byte[] fileBytes = file.getBytes();
        String storageUrl = storageService.uploadFile(
                new ByteArrayInputStream(fileBytes), file.getOriginalFilename(), file.getSize());
        String extractedMetadata = metadataService.extractMetadata(file.getOriginalFilename(), fileBytes);

        FileMetadata metadata = FileMetadata.builder()
                .originalName(file.getOriginalFilename())
                .storageUrl(storageUrl)
                .fileSize(file.getSize())
                .uploadTimestamp(LocalDateTime.now())
                .description(description)
                .uploadedBy(uploadedBy)
                .extractedMetadata(extractedMetadata)
                .build();

        FileMetadata saved = repository.save(metadata);

        return mapToResponse(saved);
    }

    public void deleteFile(UUID id) {
        FileMetadata metadata = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
        storageService.deleteFile(metadata.getStorageUrl());
        repository.deleteById(id);
    }

    public void deleteFiles(List<UUID> ids) {
        for (UUID id : ids) {
            deleteFile(id);
        }
    }

    public FileResponse updateFile(UUID id, String description) {
        FileMetadata metadata = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
        metadata.setDescription(description);
        return mapToResponse(repository.save(metadata));
    }

    public FileResponse getFileMetadata(UUID id) {
        FileMetadata metadata = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
        return mapToResponse(metadata);
    }

    public long getTotalFileCount(String currentUser, boolean isAdmin) {
        if (isAdmin) {
            return repository.count();
        }
        return repository.countByUploadedBy(currentUser);
    }

    public Map<String, Long> getFileTypeDistribution(String currentUser, boolean isAdmin) {
        List<FileMetadata> all;
        if (isAdmin) {
            all = repository.findAll();
        } else {
            all = repository.findByUploadedBy(currentUser);
        }
        Map<String, Long> dist = new java.util.HashMap<>();
        for (FileMetadata f : all) {
            String type = com.cloudlens.api.util.FileUtils.getFileIconClass(f.getOriginalName());
            dist.merge(type, 1L, Long::sum);
        }
        return dist;
    }

    private FileResponse mapToResponse(FileMetadata entity) {
        return FileResponse.builder()
                .internalId(entity.getInternalId())
                .originalName(entity.getOriginalName())
                .storageUrl(entity.getStorageUrl())
                .downloadUrl(storageService.generatePresignedUrl(entity.getStorageUrl()))
                .fileSize(entity.getFileSize())
                .uploadTimestamp(entity.getUploadTimestamp())
                .description(entity.getDescription())
                .uploadedBy(entity.getUploadedBy())
                .extractedMetadata(entity.getExtractedMetadata())
                .build();
    }
}
