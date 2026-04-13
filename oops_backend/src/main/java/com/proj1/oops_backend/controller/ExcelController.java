package com.proj1.oops_backend.controller;

import java.nio.file.Path;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.proj1.oops_backend.service.FileStorageService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ExcelController {

    private final FileStorageService fileStorageService;

    public ExcelController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "folder", required = false) String folder) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Upload failed: file is empty.");
        }

        try {
            String safeFolder = fileStorageService.sanitizeFolder(folder);
            Path targetFile = fileStorageService.store(file, safeFolder);

            return ResponseEntity.ok("Uploaded to " + safeFolder + "/" + targetFile.getFileName() + "!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Upload failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}