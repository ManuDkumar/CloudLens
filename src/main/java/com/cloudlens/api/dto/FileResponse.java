package com.cloudlens.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private UUID internalId;
    private String originalName;
    private String storageUrl;
    private String downloadUrl;
    private Long fileSize;
    private LocalDateTime uploadTimestamp;
    private String description;
    private String uploadedBy;
    private String extractedMetadata;
}
