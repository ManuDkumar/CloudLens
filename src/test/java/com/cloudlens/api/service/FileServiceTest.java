package com.cloudlens.api.service;

import com.cloudlens.api.entity.FileMetadata;
import com.cloudlens.api.exception.FileNotFoundException;
import com.cloudlens.api.repository.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private StorageService storageService;
    @Mock
    private FileMetadataRepository repository;
    @Mock
    private MetadataService metadataService;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(storageService, repository, metadataService);
    }

    @Test
    void deleteFile_ownFile_asUser_shouldSucceed() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = FileMetadata.builder()
                .internalId(id)
                .uploadedBy("alice")
                .storageUrl("test-key")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(metadata));

        fileService.deleteFile(id, "alice", false);

        verify(repository).deleteById(id);
        verify(storageService).deleteFile("test-key");
    }

    @Test
    void deleteFile_otherFile_asUser_shouldThrow() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = FileMetadata.builder()
                .internalId(id)
                .uploadedBy("bob")
                .storageUrl("test-key")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(metadata));

        assertThatThrownBy(() -> fileService.deleteFile(id, "alice", false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to delete this file");

        verify(repository, never()).deleteById(any());
        verify(storageService, never()).deleteFile(any());
    }

    @Test
    void deleteFile_otherFile_asAdmin_shouldSucceed() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = FileMetadata.builder()
                .internalId(id)
                .uploadedBy("bob")
                .storageUrl("test-key")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(metadata));

        fileService.deleteFile(id, "admin", true);

        verify(repository).deleteById(id);
        verify(storageService).deleteFile("test-key");
    }

    @Test
    void deleteFile_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileService.deleteFile(id, "alice", false))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    void updateFile_ownFile_asUser_shouldSucceed() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = FileMetadata.builder()
                .internalId(id)
                .uploadedBy("alice")
                .description("old desc")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(metadata));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        fileService.updateFile(id, "new desc", "alice", false);

        verify(repository).save(argThat(m -> m.getDescription().equals("new desc")));
    }

    @Test
    void updateFile_otherFile_asUser_shouldThrow() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = FileMetadata.builder()
                .internalId(id)
                .uploadedBy("bob")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(metadata));

        assertThatThrownBy(() -> fileService.updateFile(id, "new desc", "alice", false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to update this file");
    }

    @Test
    void uploadFile_shouldStreamToStorage() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getSize()).thenReturn(100L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));
        when(storageService.uploadFile(any(), eq("test.txt"), eq(100L))).thenReturn("stored-key");
        when(metadataService.extractMetadata(any(), any(), anyLong())).thenReturn("{}");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        fileService.uploadFile(file, "a test file", "alice");

        verify(storageService).uploadFile(any(), eq("test.txt"), eq(100L));
        verify(repository).save(argThat(m -> m.getOriginalName().equals("test.txt")));
    }

    @Test
    void getFileMetadata_ownFile_asUser_shouldSucceed() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = FileMetadata.builder()
                .internalId(id)
                .uploadedBy("alice")
                .originalName("doc.pdf")
                .fileSize(500L)
                .uploadTimestamp(LocalDateTime.now())
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(metadata));

        fileService.getFileMetadata(id, "alice", false);

        verify(repository).findById(id);
    }

    @Test
    void getDownloadUrl_ownFile_asUser_shouldGeneratePresignedUrl() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = FileMetadata.builder()
                .internalId(id)
                .uploadedBy("alice")
                .storageUrl("test-key")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(metadata));
        when(storageService.generatePresignedUrl("test-key")).thenReturn("http://presigned.url");

        String url = fileService.getDownloadUrl(id, "alice", false);

        assertThat(url).isEqualTo("http://presigned.url");
    }

    @Test
    void getDownloadUrl_otherFile_asUser_shouldThrow() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = FileMetadata.builder()
                .internalId(id)
                .uploadedBy("bob")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(metadata));

        assertThatThrownBy(() -> fileService.getDownloadUrl(id, "alice", false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getFileMetadata_otherFile_asUser_shouldThrow() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = FileMetadata.builder()
                .internalId(id)
                .uploadedBy("bob")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(metadata));

        assertThatThrownBy(() -> fileService.getFileMetadata(id, "alice", false))
                .isInstanceOf(AccessDeniedException.class);
    }
}
