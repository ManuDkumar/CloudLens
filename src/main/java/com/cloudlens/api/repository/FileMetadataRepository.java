package com.cloudlens.api.repository;

import com.cloudlens.api.entity.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
    List<FileMetadata> findAllByOrderByUploadTimestampDesc();

    Page<FileMetadata> findAllByOrderByUploadTimestampDesc(Pageable pageable);

    Page<FileMetadata> findByUploadedByOrderByUploadTimestampDesc(String uploadedBy, Pageable pageable);

    @Query("SELECT f FROM FileMetadata f WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(f.originalName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY f.uploadTimestamp DESC")
    Page<FileMetadata> searchFiles(@Param("search") String search, Pageable pageable);

    @Query("SELECT f FROM FileMetadata f WHERE " +
           "f.uploadedBy = :uploadedBy AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(f.originalName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY f.uploadTimestamp DESC")
    Page<FileMetadata> searchUserFiles(@Param("uploadedBy") String uploadedBy, @Param("search") String search, Pageable pageable);

    long countByUploadedBy(String uploadedBy);

    List<FileMetadata> findByUploadedBy(String uploadedBy);
}
