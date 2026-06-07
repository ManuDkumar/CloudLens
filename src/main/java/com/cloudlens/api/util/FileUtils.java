package com.cloudlens.api.util;

import java.util.Locale;

public final class FileUtils {

    private FileUtils() {}

    public static String getFileIconClass(String fileName) {
        if (fileName == null) return "default";
        String ext = extension(fileName);
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "ico" -> "image";
            case "pdf" -> "pdf";
            case "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "rtf" -> "document";
            case "zip", "rar", "tar", "gz", "7z", "bz2" -> "archive";
            case "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm" -> "video";
            case "mp3", "wav", "flac", "aac", "ogg", "wma" -> "audio";
            case "java", "py", "js", "ts", "html", "css", "json", "xml", "yaml", "yml", "sh", "bat", "sql" -> "code";
            default -> "default";
        };
    }

    public static String getFileIcon(String fileName) {
        if (fileName == null) return "bi bi-file-earmark";
        String ext = extension(fileName);
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "bmp", "webp", "ico", "svg" -> "bi bi-file-earmark-image";
            case "pdf" -> "bi bi-file-earmark-pdf";
            case "doc", "docx" -> "bi bi-file-earmark-word";
            case "xls", "xlsx" -> "bi bi-file-earmark-excel";
            case "ppt", "pptx" -> "bi bi-file-earmark-ppt";
            case "zip", "rar", "tar", "gz", "7z", "bz2" -> "bi bi-file-earmark-zip";
            case "txt" -> "bi bi-file-earmark-text";
            case "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm" -> "bi bi-file-earmark-play";
            case "mp3", "wav", "flac", "aac", "ogg", "wma" -> "bi bi-file-earmark-music";
            case "java", "py", "js", "ts", "html", "css", "json", "xml", "yaml", "yml", "sh", "bat" -> "bi bi-file-earmark-code";
            default -> "bi bi-file-earmark";
        };
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot == -1 ? "" : fileName.substring(dot);
    }

    public static String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "-";
        long abs = Math.abs(bytes);
        String unit;
        double value;
        if (abs >= 1_073_741_824L) {
            value = bytes / 1_073_741_824.0;
            unit = "GB";
        } else if (abs >= 1_048_576) {
            value = bytes / 1_048_576.0;
            unit = "MB";
        } else if (abs >= 1_024) {
            value = bytes / 1_024.0;
            unit = "KB";
        } else {
            value = bytes;
            unit = "B";
        }
        return String.format("%.1f %s", value, unit);
    }

    public static boolean isImage(String fileName) {
        if (fileName == null) return false;
        String ext = extension(fileName);
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "ico" -> true;
            default -> false;
        };
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot == -1 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
