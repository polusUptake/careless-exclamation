package com.proj1.oops_backend.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import com.proj1.oops_backend.model.RawRow;
import com.proj1.oops_backend.model.Report;
import com.proj1.oops_backend.repository.ReportRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.proj1.oops_backend.service.CalculationService;
import com.proj1.oops_backend.service.ExcelExportService;
import com.proj1.oops_backend.service.ExcelReaderService;
import com.proj1.oops_backend.service.FileStorageService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ExcelController {

    private final FileStorageService fileStorageService;
    private final ExcelReaderService excelReaderService;
    private final CalculationService calculationService;
    private final ReportRepository reportRepository;
    private final ExcelExportService excelExportService;

    public ExcelController(
            FileStorageService fileStorageService,
            ExcelReaderService excelReaderService,
            CalculationService calculationService,
            ReportRepository reportRepository,
            ExcelExportService excelExportService) {
        this.fileStorageService = fileStorageService;
        this.excelReaderService = excelReaderService;
        this.calculationService = calculationService;
        this.reportRepository = reportRepository;
        this.excelExportService = excelExportService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Upload failed: file is empty.");
        }

        try {
            Path targetFile = fileStorageService.store(file);
            List<RawRow> rawRows = excelReaderService.readExcel(targetFile);
            List<Report> calculatedReports = calculationService.calculate(rawRows, targetFile.getFileName().toString());
            List<Report> savedReports = reportRepository.saveAll(calculatedReports);
            Path exportPath = excelExportService.export(savedReports);
            String downloadPath = "/download/" + exportPath.getFileName();

            return ResponseEntity.ok(Map.of(
                    "message", "File processed successfully.",
                    "uploadedFile", targetFile.getFileName().toString(),
                    "rawRowCount", rawRows.size(),
                    "savedReportCount", savedReports.size(),
                    "downloadPath", downloadPath,
                    "reports", savedReports));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Upload failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        try {
            Path filePath = Path.of("uploads", fileName).normalize();

            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}