package com.cloudlens.api.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MetadataService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractMetadata(String fileName, byte[] fileBytes) {
        if (fileName == null || fileBytes == null || fileBytes.length == 0) {
            return "{}";
        }
        String ext = extension(fileName);
        try {
            switch (ext) {
                case "jpg": case "jpeg": case "png": case "gif":
                case "bmp": case "webp": case "ico": case "tiff": case "tif":
                    return extractImageMetadata(fileBytes);
                default:
                    return extractBasicMetadata(fileBytes.length, ext);
            }
        } catch (Exception e) {
            return "{\"error\":\"Failed to extract metadata\"}";
        }
    }

    private String extractImageMetadata(byte[] fileBytes) {
        try {
            Map<String, String> metadata = new LinkedHashMap<>();
            com.drew.metadata.Metadata drewMetadata = ImageMetadataReader.readMetadata(
                    new ByteArrayInputStream(fileBytes));

            metadata.put("File Type", "Image");

            for (Directory directory : drewMetadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    String key = tag.getDirectoryName() + " - " + tag.getTagName();
                    String value = tag.getDescription();
                    if (value != null && !value.isBlank() && !key.contains("Unknown")) {
                        metadata.put(key, value);
                    }
                }
            }

            ObjectNode root = objectMapper.valueToTree(metadata);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            try {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("File Type", "Image");
                node.put("File Size", formatSize(fileBytes.length));
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
            } catch (JsonProcessingException ex) {
                return "{}";
            }
        }
    }

    private String extractBasicMetadata(long fileSize, String ext) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("File Type", fileTypeLabel(ext));
            node.put("File Size", formatSize(fileSize));
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String fileTypeLabel(String ext) {
        return switch (ext.toLowerCase()) {
            case "pdf" -> "PDF Document";
            case "doc", "docx" -> "Word Document";
            case "xls", "xlsx" -> "Excel Spreadsheet";
            case "ppt", "pptx" -> "PowerPoint Presentation";
            case "txt" -> "Text File";
            case "csv" -> "CSV File";
            case "zip", "rar", "tar", "gz", "7z" -> "Archive";
            case "mp4", "avi", "mkv", "mov", "webm" -> "Video";
            case "mp3", "wav", "flac", "aac", "ogg" -> "Audio";
            case "java", "py", "js", "ts", "html", "css", "json", "xml" -> "Source Code";
            default -> "Unknown";
        };
    }

    private String formatSize(long bytes) {
        long abs = Math.abs(bytes);
        if (abs >= 1_073_741_824L) return String.format("%.1f GB", bytes / 1_073_741_824.0);
        if (abs >= 1_048_576) return String.format("%.1f MB", bytes / 1_048_576.0);
        if (abs >= 1_024) return String.format("%.1f KB", bytes / 1_024.0);
        return bytes + " B";
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot == -1 ? "" : fileName.substring(dot + 1).toLowerCase();
    }
}
