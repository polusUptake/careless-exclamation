package com.proj1.oops_backend.service;

import com.proj1.oops_backend.model.WorkbookInput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

@Service
public class ExcelReaderService {

    private static final List<String> REQUIRED_ACADEMIC_SHEETS = List.of("it1", "it2", "it3", "sem");
    private static final String CES_SHEET = "ces";

    public WorkbookInput readExcel(Path filePath) throws IOException {
        if (filePath == null || !Files.exists(filePath)) {
            throw new IllegalArgumentException("Excel file not found.");
        }

        DataFormatter formatter = new DataFormatter();

        try (InputStream fis = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {
            validateSheetOrder(workbook);
            Map<String, WorkbookInput.AcademicSheet> academicSheets = new LinkedHashMap<>();
            List<String> academicOrder = new ArrayList<>(REQUIRED_ACADEMIC_SHEETS);

            for (String sheetName : REQUIRED_ACADEMIC_SHEETS) {
                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new IllegalArgumentException("Missing required sheet: " + sheetName);
                }
                academicSheets.put(sheetName, parseAcademicSheet(sheet, formatter));
            }

            Set<String> knownCoCodes = collectCoCodes(academicSheets);
            Sheet ces = workbook.getSheet(CES_SHEET);
            if (ces == null) {
                throw new IllegalArgumentException("Missing required sheet: " + CES_SHEET);
            }
            WorkbookInput.CesSheet cesSheet = parseCesSheet(ces, formatter, knownCoCodes, academicSheets.get("sem"));

            return new WorkbookInput(academicSheets, academicOrder, cesSheet);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to read Excel file: " + e.getMessage(), e);
        }
    }

    private void validateSheetOrder(Workbook workbook) {
        if (workbook.getNumberOfSheets() < 5) {
            throw new IllegalArgumentException("Workbook must contain 5 sheets in order: it1, it2, it3, sem, ces.");
        }

        List<String> expected = List.of("it1", "it2", "it3", "sem", "ces");
        for (int i = 0; i < expected.size(); i++) {
            String actual = workbook.getSheetName(i);
            if (!expected.get(i).equalsIgnoreCase(actual)) {
                throw new IllegalArgumentException("Sheet order must be it1, it2, it3, sem, ces.");
            }
        }
    }

    private WorkbookInput.AcademicSheet parseAcademicSheet(Sheet sheet, DataFormatter formatter) {
        Row maxRow = sheet.getRow(1);
        Row coRow = sheet.getRow(2);
        Row headerRow = sheet.getRow(3);

        if (maxRow == null || coRow == null || headerRow == null) {
            throw new IllegalArgumentException("Sheet " + sheet.getSheetName() + " is missing required structure (Max Marks, COs, Headers).");
        }

        int questionStartCol = 3; 
        int totalCol = resolveAcademicTotalColumn(headerRow, questionStartCol, formatter);
        
        if (totalCol <= questionStartCol) {
            throw new IllegalArgumentException("Sheet " + sheet.getSheetName() + " has no question columns.");
        }

        int questionCount = totalCol - questionStartCol;
        List<WorkbookInput.Question> questions = new ArrayList<>();
        
        for (int q = 0; q < questionCount; q++) {
            int col = questionStartCol + q;
            double maxMarks = parseNumericCell(maxRow.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), formatter);
            String coCode = formatter.formatCellValue(coRow.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
            if (coCode.isBlank()) {
                coCode = "CO_UNMAPPED";
            }
            questions.add(new WorkbookInput.Question("Q" + (q + 1), coCode, maxMarks));
        }

        List<WorkbookInput.StudentMarks> students = new ArrayList<>();

        for (int r = 4; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            String rollNo = formatter.formatCellValue(row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
            String name = formatter.formatCellValue(row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();

            if (rollNo.isBlank() && name.isBlank()) continue;

            List<Double> marks = new ArrayList<>();
            for (int q = 0; q < questionCount; q++) {
                int col = questionStartCol + q;
                marks.add(parseNumericCell(row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), formatter));
            }

            double totalMarks = 0.0;
            if (totalCol < row.getLastCellNum()) {
                totalMarks = parseNumericCell(row.getCell(totalCol, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), formatter);
            }
            if (totalMarks == 0.0) {
                totalMarks = marks.stream().mapToDouble(Double::doubleValue).sum();
            }

            students.add(new WorkbookInput.StudentMarks(rollNo, name, marks, totalMarks));
        }

        return new WorkbookInput.AcademicSheet(sheet.getSheetName(), questions, students);
    }

    private int resolveAcademicTotalColumn(Row headerRow, int questionStartCol, DataFormatter formatter) {
        int lastCell = headerRow.getLastCellNum();
        int fallbackCol = questionStartCol;
        
        for (int col = questionStartCol; col < lastCell; col++) {
            String value = formatter.formatCellValue(headerRow.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
            
            if ("total".equalsIgnoreCase(value) || "tot".equalsIgnoreCase(value) || "total marks".equalsIgnoreCase(value)) {
                return col;
            }

            if (!value.isBlank()) {
                fallbackCol = col + 1;
            }
        }

        return fallbackCol; 
    }

    private Set<String> collectCoCodes(Map<String, WorkbookInput.AcademicSheet> sheets) {
        Set<String> coCodes = new LinkedHashSet<>();
        for (WorkbookInput.AcademicSheet sheet : sheets.values()) {
            for (WorkbookInput.Question question : sheet.questions()) {
                if (!question.coCode().isBlank() && !question.coCode().equals("CO_UNMAPPED")) {
                    coCodes.add(question.coCode());
                }
            }
        }
        return coCodes;
    }

    private WorkbookInput.CesSheet parseCesSheet(
            Sheet sheet,
            DataFormatter formatter,
            Set<String> knownCoCodes,
            WorkbookInput.AcademicSheet semSheet) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalArgumentException("CES sheet is empty.");
        }

        int questionStartCol = 3; 

        int lastCell = header.getLastCellNum();
        int questionCount = 0;
        for(int col = questionStartCol; col < lastCell; col++) {
            String val = formatter.formatCellValue(header.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
            if(!val.isBlank()) {
                questionCount++;
            } else {
                break;
            }
        }

        if (questionCount == 0) {
            throw new IllegalArgumentException("CES sheet has no question columns.");
        }

        int studentStartRow = 1;
        Row optionalCoRow = sheet.getRow(1);
        boolean hasExplicitCoRow = looksLikeCoRow(optionalCoRow, formatter, questionStartCol, questionCount);
        if (hasExplicitCoRow) {
            studentStartRow = 2;
        }

        List<WorkbookInput.CesQuestion> questions = new ArrayList<>();
        for (int q = 0; q < questionCount; q++) {
            int col = questionStartCol + q;
            String questionLabel = formatter.formatCellValue(header.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
            if (questionLabel.isBlank()) {
                questionLabel = "Q" + (q + 1);
            }

            String mappedCo;
            if (hasExplicitCoRow) {
                mappedCo = formatter.formatCellValue(optionalCoRow.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
            } else {
                mappedCo = resolveCesCoFromHeader(questionLabel, q, knownCoCodes, semSheet);
            }
            if (mappedCo.isBlank()) {
                mappedCo = "CO_UNMAPPED";
            }

            questions.add(new WorkbookInput.CesQuestion(questionLabel, mappedCo));
        }

        List<WorkbookInput.CesResponse> students = new ArrayList<>();
        for (int r = studentStartRow; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            String rollNo = formatter.formatCellValue(row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
            String name = formatter.formatCellValue(row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
            if (rollNo.isBlank() && name.isBlank()) continue;

            List<String> responses = new ArrayList<>();
            for (int q = 0; q < questionCount; q++) {
                int col = questionStartCol + q;
                String response = formatter.formatCellValue(row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
                responses.add(response);
            }

            students.add(new WorkbookInput.CesResponse(rollNo, name, responses));
        }

        return new WorkbookInput.CesSheet(sheet.getSheetName(), questions, students);
    }

    private boolean looksLikeCoRow(Row row, DataFormatter formatter, int questionStartCol, int questionCount) {
        if (row == null) return false;

        int coLike = 0;
        int responseLike = 0;
        for (int q = 0; q < questionCount; q++) {
            String value = formatter
                    .formatCellValue(row.getCell(questionStartCol + q, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
                    .trim();
            String upper = value.toUpperCase(Locale.ROOT);
            if (upper.startsWith("CO") || upper.startsWith("CE")) {
                coLike++;
            }
            if (isCesPositiveResponse(upper) || "NEUTRAL".equals(upper) || "DISAGREE".equals(upper)) {
                responseLike++;
            }
        }
        return coLike > 0 && responseLike == 0;
    }

    private String resolveCesCoFromHeader(
            String headerLabel,
            int questionIndex,
            Set<String> knownCoCodes,
            WorkbookInput.AcademicSheet semSheet) {
        if (knownCoCodes.contains(headerLabel)) {
            return headerLabel;
        }

        String normalized = headerLabel.toUpperCase(Locale.ROOT);
        if (knownCoCodes.contains(normalized)) {
            return normalized;
        }

        if (semSheet != null && questionIndex < semSheet.questions().size()) {
            return semSheet.questions().get(questionIndex).coCode();
        }

        return "CO_UNMAPPED";
    }

    private double parseNumericCell(Cell cell, DataFormatter formatter) {
        String text = formatter.formatCellValue(cell).trim();
        if (text.isBlank()) return 0.0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private boolean isCesPositiveResponse(String responseUpper) {
        return "AGREE".equals(responseUpper)
                || "STRONGLY AGREE".equals(responseUpper)
                || "STRONGLY_AGREE".equals(responseUpper);
    }
}