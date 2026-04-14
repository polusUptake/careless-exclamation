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

	public Path store(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File is empty.");
		}

		String original = file.getOriginalFilename();
		if (original == null || original.isBlank()) {
			throw new IllegalArgumentException("Invalid filename.");
		}

		String safeFileName = Path.of(original).getFileName().toString();
		Path uploadDir = Path.of("uploads");
		Files.createDirectories(uploadDir);

		Path target = uploadDir.resolve(safeFileName);
		try (InputStream inputStream = file.getInputStream()) {
			Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
		}
		return target;
	}
}
