package com.cloudlens.api.service;

import com.cloudlens.api.dto.FileResponse;
import com.cloudlens.api.entity.FileMetadata;
import com.cloudlens.api.exception.FileNotFoundException;
import com.cloudlens.api.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
        String originalName = file.getOriginalFilename();
        String storageUrl = storageService.uploadFile(file.getInputStream(), originalName, file.getSize());
        String extractedMetadata;
        try (InputStream metaStream = file.getInputStream()) {
            extractedMetadata = metadataService.extractMetadata(originalName, metaStream, file.getSize());
        }

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

    public void deleteFile(UUID id, String currentUser, boolean isAdmin) {
        FileMetadata metadata = repository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));
        if (!isAdmin && !metadata.getUploadedBy().equals(currentUser)) {
            throw new AccessDeniedException("You do not have permission to delete this file");
        }
        storageService.deleteFile(metadata.getStorageUrl());
        repository.deleteById(id);
    }

    public void deleteFiles(List<UUID> ids, String currentUser, boolean isAdmin) {
        for (UUID id : ids) {
            deleteFile(id, currentUser, isAdmin);
        }
    }

    public void deleteAllFilesByUser(String username) {
        List<FileMetadata> files = repository.findByUploadedBy(username);
        for (FileMetadata file : files) {
            storageService.deleteFile(file.getStorageUrl());
        }
        repository.deleteAll(files);
    }

    public FileResponse updateFile(UUID id, String description, String currentUser, boolean isAdmin) {
        FileMetadata metadata = repository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));
        if (!isAdmin && !metadata.getUploadedBy().equals(currentUser)) {
            throw new AccessDeniedException("You do not have permission to update this file");
        }
        metadata.setDescription(description);
        return mapToResponse(repository.save(metadata));
    }

    public FileResponse getFileMetadata(UUID id, String currentUser, boolean isAdmin) {
        FileMetadata metadata = repository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));
        if (!isAdmin && !metadata.getUploadedBy().equals(currentUser)) {
            throw new AccessDeniedException("You do not have permission to access this file");
        }
        return mapToResponse(metadata);
    }

    public long getTotalFileCount(String currentUser, boolean isAdmin) {
        if (isAdmin) {
            return repository.count();
        }
        return repository.countByUploadedBy(currentUser);
    }

    public Map<String, Long> getFileTypeDistribution(String currentUser, boolean isAdmin) {
        List<String> names = repository.findOriginalNamesByUsername(isAdmin ? null : currentUser);
        Map<String, Long> dist = new java.util.HashMap<>();
        for (String name : names) {
            String type = com.cloudlens.api.util.FileUtils.getFileIconClass(name);
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
