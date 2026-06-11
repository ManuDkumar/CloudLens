package com.cloudlens.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_metadata", indexes = {
    @Index(name = "idx_uploaded_by", columnList = "uploadedBy"),
    @Index(name = "idx_original_name", columnList = "originalName"),
    @Index(name = "idx_upload_timestamp", columnList = "uploadTimestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID internalId;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storageUrl;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private LocalDateTime uploadTimestamp;

    private String description;

    private String uploadedBy;

    @Column(columnDefinition = "TEXT")
    private String extractedMetadata;
}
