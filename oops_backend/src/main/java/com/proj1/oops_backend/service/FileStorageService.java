package com.proj1.oops_backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

	public Path store(MultipartFile file, String folder) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File is empty.");
		}

		String safeFolder = sanitizeFolder(folder);
		String original = file.getOriginalFilename();
		if (original == null || original.isBlank()) {
			throw new IllegalArgumentException("Invalid filename.");
		}

		String safeFileName = Path.of(original).getFileName().toString();
		Path uploadDir = Path.of("uploads", safeFolder);
		Files.createDirectories(uploadDir);

		Path target = uploadDir.resolve(safeFileName);
		try (InputStream inputStream = file.getInputStream()) {
			Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
		}
		return target;
	}

	public String sanitizeFolder(String folder) {
		if (folder == null || folder.isBlank()) {
			return "general";
		}
		String cleaned = folder.replaceAll("[^a-zA-Z0-9_-]", "");
		return cleaned.isBlank() ? "general" : cleaned;
	}
}
