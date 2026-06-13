package com.cloudlens.api.controller;

import com.cloudlens.api.dto.FileResponse;
import com.cloudlens.api.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final FileService fileService;

    @GetMapping("/")
    public String index(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Authentication authentication,
            Model model) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Page<FileResponse> filePage = fileService.getAllFiles(page, size, search, username, isAdmin);
        model.addAttribute("files", filePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", filePage.getTotalPages());
        model.addAttribute("totalItems", filePage.getTotalElements());
        model.addAttribute("search", search);

        model.addAttribute("totalFileCount", fileService.getTotalFileCount(username, isAdmin));
        model.addAttribute("fileTypeDistribution", fileService.getFileTypeDistribution(username, isAdmin));
        return isAdmin ? "admin" : "index";
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            fileService.uploadFile(file, description, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "File uploaded successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable UUID id, Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            fileService.deleteFile(id, username, isAdmin);
            redirectAttributes.addFlashAttribute("message", "File deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/update/{id}")
    public String updateFile(@PathVariable UUID id,
                             @RequestParam("description") String description,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            fileService.updateFile(id, description, username, isAdmin);
            redirectAttributes.addFlashAttribute("message", "Description updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Update failed: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/delete/bulk")
    public String deleteBulk(@RequestParam("selectedIds") List<UUID> selectedIds,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            fileService.deleteFiles(selectedIds, username, isAdmin);
            redirectAttributes.addFlashAttribute("message", selectedIds.size() + " file(s) deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Bulk delete failed: " + e.getMessage());
        }
        return "redirect:/";
    }
}
