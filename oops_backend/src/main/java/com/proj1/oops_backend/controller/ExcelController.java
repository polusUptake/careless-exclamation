package com.proj1.oops_backend.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ExcelController {

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Upload failed: file is empty.");
        }

        try {
            Path uploadDir = Path.of("uploads");
            Files.createDirectories(uploadDir);

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Upload failed: invalid filename.");
            }

            file.transferTo(new File(uploadDir.toFile(), originalFileName));
            return ResponseEntity.ok("Uploaded!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed.");
        }
    }
}