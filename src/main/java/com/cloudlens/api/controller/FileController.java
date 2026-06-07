package com.cloudlens.api.controller;

import com.cloudlens.api.dto.FileResponse;
import com.cloudlens.api.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description,
            Authentication authentication) {
        try {
            FileResponse response = fileService.uploadFile(file, description, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileResponse> updateFile(
            @PathVariable UUID id,
            @RequestParam("description") String description) {
        try {
            FileResponse response = fileService.updateFile(id, description);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        try {
            fileService.deleteFile(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDelete(@RequestBody List<UUID> ids) {
        fileService.deleteFiles(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFile(@PathVariable UUID id) {
        try {
            FileResponse response = fileService.getFileMetadata(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
