package com.proj1.oops_backend.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import com.proj1.oops_backend.model.CalculationResult;
import com.proj1.oops_backend.model.Report;
import com.proj1.oops_backend.model.WorkbookInput;
import com.proj1.oops_backend.repository.ReportRepository;
import jakarta.transaction.Transactional;
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
    @Transactional
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "facultyName", required = false) String facultyName,
            @RequestParam(value = "courseName", required = false) String courseName) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Upload failed: file is empty.");
        }

        String resolvedFacultyName = facultyName == null ? "" : facultyName.trim();
        String resolvedCourseName = courseName == null ? "" : courseName.trim();
        if (resolvedFacultyName.isBlank() || resolvedCourseName.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Upload failed: faculty name and course name are required.");
        }

        try {
            Path targetFile = fileStorageService.store(file);
            WorkbookInput workbookInput = excelReaderService.readExcel(targetFile);
            CalculationResult calculationResult = calculationService.calculate(workbookInput);
            List<Report> calculatedReports = calculationService.toReports(
                calculationResult,
                targetFile.getFileName().toString());

                // Keep only the latest upload's calculated rows while preserving the report table itself.
                reportRepository.deleteAllInBatch();
            List<Report> savedReports = reportRepository.saveAll(calculatedReports);
            Path exportPath = excelExportService.export(calculationResult, workbookInput, resolvedFacultyName, resolvedCourseName);
            String downloadPath = "/download/" + exportPath.getFileName();

            return ResponseEntity.ok(Map.of(
                    "message", "File processed successfully.",
                    "uploadedFile", targetFile.getFileName().toString(),
                "studentCount", calculationResult.studentRows().size(),
                "coCount", calculationResult.coCodes().size(),
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