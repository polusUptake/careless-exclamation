package com.proj1.oops_backend.service;

import com.proj1.oops_backend.model.CalculationResult;
import com.proj1.oops_backend.model.WorkbookInput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelExportService {

    private static final List<String> ACADEMIC_SHEET_ORDER = List.of("it1", "it2", "it3", "sem");

    public Path export(CalculationResult result, WorkbookInput workbookInput, String facultyName, String courseName) throws IOException {
        Path exportDir = Path.of("uploads");
        Files.createDirectories(exportDir);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path exportFile = exportDir.resolve("calculated_" + timestamp + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream outputStream = Files.newOutputStream(exportFile)) {
            writeStudentPerformanceSheet(workbook, result, workbookInput, facultyName, courseName);
            writeCesSummarySheet(workbook, result, facultyName, courseName);
            writeFinalAttainmentSheet(workbook, result, facultyName, courseName);
            
            // Generate the 4th sheet for PO Attainment
            writePoAttainmentSheet(workbook, result, facultyName, courseName);
            
            workbook.write(outputStream);
        }

        return exportFile;
    }

    private void writeStudentPerformanceSheet(
            Workbook workbook,
            CalculationResult result,
            WorkbookInput input,
            String facultyName,
            String courseName) {
        Sheet sheet = workbook.createSheet("Student Performance");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowIndex = writeSheetMetadata(sheet, facultyName, courseName, headerStyle, dataStyle);
        
        Row row0 = sheet.createRow(rowIndex++);
        Row row1 = sheet.createRow(rowIndex++);
        Row row2 = sheet.createRow(rowIndex++);
        Row row3 = sheet.createRow(rowIndex++);

        createStyledCell(row1, 2, "Marks", headerStyle);
        createStyledCell(row2, 2, "CO", headerStyle);

        int col = 0;
        createStyledCell(row3, col++, "SrNo", headerStyle);
        createStyledCell(row3, col++, "Roll No", headerStyle);
        createStyledCell(row3, col++, "Name", headerStyle);

        for (String sheetName : ACADEMIC_SHEET_ORDER) {
            WorkbookInput.AcademicSheet acSheet = input.academicSheets().get(sheetName);
            if (acSheet == null) continue;

            int startCol = col;
            double totalMaxMarks = 0;

            for (WorkbookInput.Question q : acSheet.questions()) {
                createStyledCell(row0, col, "", headerStyle); 
                createStyledCell(row1, col, q.maxMarks(), headerStyle);
                createStyledCell(row2, col, q.coCode(), headerStyle);
                createStyledCell(row3, col, q.questionId(), headerStyle);
                totalMaxMarks += q.maxMarks();
                col++;
            }
            
            createStyledCell(row0, col, "", headerStyle); 
            createStyledCell(row1, col, totalMaxMarks, headerStyle); 
            createStyledCell(row2, col, "", headerStyle); 
            createStyledCell(row3, col, "Total", headerStyle);
            
            createStyledCell(row0, startCol, sheetName.toUpperCase() + " MARKS", headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(row0.getRowNum(), row0.getRowNum(), startCol, col));
            col++;
        }

        int cesStart = col;
        for (WorkbookInput.CesQuestion q : input.cesSheet().questions()) {
            createStyledCell(row0, col, "", headerStyle);
            createStyledCell(row1, col, "", headerStyle); 
            createStyledCell(row2, col, q.coCode(), headerStyle);
            createStyledCell(row3, col, q.questionId(), headerStyle); 
            col++;
        }
        createStyledCell(row0, cesStart, "CES RESPONSES", headerStyle);
        if (cesStart < col - 1) {
            sheet.addMergedRegion(new CellRangeAddress(row0.getRowNum(), row0.getRowNum(), cesStart, col - 1));
        }

        int coTotalStart = col;
        for (String co : result.coCodes()) {
            createStyledCell(row0, col, "", headerStyle);
            createStyledCell(row1, col, "", headerStyle);
            createStyledCell(row2, col, "", headerStyle);
            createStyledCell(row3, col, co, headerStyle);
            col++;
        }
        createStyledCell(row0, coTotalStart, "CO TOTALS", headerStyle);
        if (coTotalStart < col - 1) {
            sheet.addMergedRegion(new CellRangeAddress(row0.getRowNum(), row0.getRowNum(), coTotalStart, col - 1));
        }

        int coAttainStart = col;
        for (String co : result.coCodes()) {
            createStyledCell(row0, col, "", headerStyle);
            createStyledCell(row1, col, "", headerStyle);
            createStyledCell(row2, col, "", headerStyle);
            createStyledCell(row3, col, co, headerStyle);
            col++;
        }
        createStyledCell(row0, coAttainStart, "CO ATTAINMENT (0/1)", headerStyle);
        if (coAttainStart < col - 1) {
            sheet.addMergedRegion(new CellRangeAddress(row0.getRowNum(), row0.getRowNum(), coAttainStart, col - 1));
        }

        int srNo = 1;
        for (CalculationResult.StudentPerformanceRow student : result.studentRows()) {
            Row row = sheet.createRow(rowIndex++);
            int c = 0;
            createStyledCell(row, c++, srNo++, dataStyle);
            createStyledCell(row, c++, student.rollNo(), dataStyle);
            createStyledCell(row, c++, student.studName(), dataStyle);

            for (String markCol : result.academicMarkColumns()) {
                createStyledCell(row, c++, student.marksByColumn().getOrDefault(markCol, 0.0), dataStyle);
            }
            for (String co : result.coCodes()) {
                createStyledCell(row, c++, student.coTotals().getOrDefault(co, 0.0), dataStyle);
            }
            for (String co : result.coCodes()) {
                createStyledCell(row, c++, student.coAttainment().getOrDefault(co, 0), dataStyle);
            }
        }

        rowIndex += 2;

        createStyledCell(sheet.createRow(rowIndex++), 0, "CO Summary", headerStyle);

        Row summaryHeader = sheet.createRow(rowIndex++);
        String[] sumHeaders = {"CO", "Students Attained", "Students Attempted", "Percentage", "Level"};
        for(int i = 0; i < sumHeaders.length; i++) {
            createStyledCell(summaryHeader, i, sumHeaders[i], headerStyle);
        }

        for (CalculationResult.CoSummaryRow co : result.coSummaryRows()) {
            Row row = sheet.createRow(rowIndex++);
            createStyledCell(row, 0, co.coCode(), dataStyle);
            createStyledCell(row, 1, co.studentsAttained(), dataStyle);
            createStyledCell(row, 2, co.studentsAttempted(), dataStyle);
            createStyledCell(row, 3, round2(co.percentage()), dataStyle);
            createStyledCell(row, 4, co.level(), dataStyle);
        }

        autoSizeColumns(sheet, Math.max(col, 5));
    }

    private void writeCesSummarySheet(Workbook workbook, CalculationResult result, String facultyName, String courseName) {
        Sheet sheet = workbook.createSheet("CES Summary");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        int rowIndex = writeSheetMetadata(sheet, facultyName, courseName, headerStyle, dataStyle);

        Row header = sheet.createRow(rowIndex++);
        createStyledCell(header, 0, "CO", headerStyle);
        createStyledCell(header, 1, "Agree+StronglyAgree", headerStyle);
        createStyledCell(header, 2, "Total Students", headerStyle);
        createStyledCell(header, 3, "Percentage", headerStyle);
        createStyledCell(header, 4, "Level", headerStyle);

        for (CalculationResult.CesSummaryRow rowData : result.cesSummaryRows()) {
            Row row = sheet.createRow(rowIndex++);
            createStyledCell(row, 0, rowData.coCode(), dataStyle);
            createStyledCell(row, 1, rowData.agreeAndStronglyAgree(), dataStyle);
            createStyledCell(row, 2, rowData.totalStudents(), dataStyle);
            createStyledCell(row, 3, round2(rowData.percentage()), dataStyle);
            createStyledCell(row, 4, rowData.level(), dataStyle);
        }

        autoSizeColumns(sheet, 5);
    }

    private void writeFinalAttainmentSheet(Workbook workbook, CalculationResult result, String facultyName, String courseName) {
        Sheet sheet = workbook.createSheet("Final CO Attainment");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        int rowIndex = writeSheetMetadata(sheet, facultyName, courseName, headerStyle, dataStyle);

        Row topHeader = sheet.createRow(rowIndex++);
        createStyledCell(topHeader, 0, "CO", headerStyle);
        createStyledCell(topHeader, 1, "DIRECT (80%) - Internal Test (50%)", headerStyle);
        createStyledCell(topHeader, 2, "DIRECT (80%) - End Sem (50%)", headerStyle);
        createStyledCell(topHeader, 3, "DIRECT Attainment", headerStyle);
        createStyledCell(topHeader, 4, "INDIRECT (20%) - CES", headerStyle);
        createStyledCell(topHeader, 5, "Level (3/2/1/0)", headerStyle);
        createStyledCell(topHeader, 6, "Final Attainment", headerStyle);

        Row subHeader = sheet.createRow(rowIndex++);
        createStyledCell(subHeader, 1, "Percentage / Level", headerStyle);
        createStyledCell(subHeader, 2, "Percentage / Level", headerStyle);
        createStyledCell(subHeader, 4, "Percentage / Level", headerStyle);

        for (CalculationResult.FinalAttainmentRow rowData : result.finalAttainmentRows()) {
            Row row = sheet.createRow(rowIndex++);
            createStyledCell(row, 0, rowData.coCode(), dataStyle);
            createStyledCell(row, 1, String.format(Locale.ROOT, "%.2f%% / L%d", rowData.internalPercentage(), rowData.internalLevel()), dataStyle);
            createStyledCell(row, 2, String.format(Locale.ROOT, "%.2f%% / L%d", rowData.semPercentage(), rowData.semLevel()), dataStyle);
            createStyledCell(row, 3, round2(rowData.directAttainment()), dataStyle);
            createStyledCell(row, 4, String.format(Locale.ROOT, "%.2f%% / L%d", rowData.cesPercentage(), rowData.cesLevel()), dataStyle);
            createStyledCell(row, 5, rowData.finalLevel(), dataStyle);
            createStyledCell(row, 6, round2(rowData.finalAttainment()), dataStyle);
        }

        autoSizeColumns(sheet, 7);
    }

    private void writePoAttainmentSheet(Workbook workbook, CalculationResult result, String facultyName, String courseName) {
        Sheet sheet = workbook.createSheet("PO Attainment");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowIndex = writeSheetMetadata(sheet, facultyName, courseName, headerStyle, dataStyle);

        Row headerRow = sheet.createRow(rowIndex++);
        createStyledCell(headerRow, 0, "CO Code", headerStyle);
        createStyledCell(headerRow, 1, "Final Attainment", headerStyle);
        for (int i = 1; i <= 12; i++) {
            createStyledCell(headerRow, i + 1, "PO" + i, headerStyle);
        }
        createStyledCell(headerRow, 14, "PSO1", headerStyle);
        createStyledCell(headerRow, 15, "PSO2", headerStyle);

        int[][] POMapping = {
            {3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0},
            {3, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0},
            {3, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0},
            {2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0}
        };

        int numPOs = 14;
        double[] sumWeighted = new double[numPOs];
        double[] relativeSum = new double[numPOs];
        int[] poMapCount = new int[numPOs];

        List<CalculationResult.FinalAttainmentRow> rows = result.finalAttainmentRows();
        for (int r = 0; r < rows.size(); r++) {
            CalculationResult.FinalAttainmentRow co = rows.get(r);
            Row row = sheet.createRow(rowIndex++);
            createStyledCell(row, 0, co.coCode(), dataStyle);

            double coAttainmentValue = co.finalAttainment();
            createStyledCell(row, 1, round2(coAttainmentValue), dataStyle);

            int[] mapping = POMapping[r % POMapping.length];

            for (int p = 0; p < numPOs; p++) {
                int mappingLevel = mapping[p];
                if (mappingLevel > 0) {
                    createStyledCell(row, p + 2, mappingLevel, dataStyle);
                    
                    //Calculate weighted contribution
                    double weighted = (mappingLevel / 3.0) * coAttainmentValue;
                    
                    //Add to PO total
                    sumWeighted[p] += weighted;
                    relativeSum[p] += mappingLevel;
                    poMapCount[p]++;
                } else {
                    createStyledCell(row, p + 2, "-", dataStyle);
                }
            }
        }

        rowIndex++;

        Row sumRow = sheet.createRow(rowIndex++);
        createStyledCell(sumRow, 0, "Total Contribution (Sum)", headerStyle);
        createStyledCell(sumRow, 1, "", headerStyle);

        Row relSumRow = sheet.createRow(rowIndex++);
        createStyledCell(relSumRow, 0, "Relative Sum", headerStyle);
        createStyledCell(relSumRow, 1, "", headerStyle);

        Row avgRow = sheet.createRow(rowIndex++);
        createStyledCell(avgRow, 0, "Average Weighted", headerStyle);
        createStyledCell(avgRow, 1, "", headerStyle);

        Row ratioRow = sheet.createRow(rowIndex++);
        createStyledCell(ratioRow, 0, "Ratio (Total / Rel Sum)", headerStyle);
        createStyledCell(ratioRow, 1, "", headerStyle);

        Row finalRow = sheet.createRow(rowIndex++);
        createStyledCell(finalRow, 0, "PO Attainment", headerStyle);
        createStyledCell(finalRow, 1, "", headerStyle);

        for (int p = 0; p < numPOs; p++) {
            int col = p + 2;
            if (poMapCount[p] > 0) {
                createStyledCell(sumRow, col, round2(sumWeighted[p]), dataStyle);
                createStyledCell(relSumRow, col, round2(relativeSum[p]), dataStyle);
                
                double avg = sumWeighted[p] / poMapCount[p];
                createStyledCell(avgRow, col, round2(avg), dataStyle);

                double ratio = sumWeighted[p] / relativeSum[p];
                createStyledCell(ratioRow, col, round2(ratio), dataStyle);

                double finalAttainment = avg * ratio;
                createStyledCell(finalRow, col, round2(finalAttainment), dataStyle);
            } else {
                createStyledCell(sumRow, col, "-", dataStyle);
                createStyledCell(relSumRow, col, "-", dataStyle);
                createStyledCell(avgRow, col, "-", dataStyle);
                createStyledCell(ratioRow, col, "-", dataStyle);
                createStyledCell(finalRow, col, "-", dataStyle);
            }
        }

        autoSizeColumns(sheet, 16);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createStyledCell(Row row, int column, String value, CellStyle style) {
        row.createCell(column).setCellValue(value);
        row.getCell(column).setCellStyle(style);
    }

    private void createStyledCell(Row row, int column, double value, CellStyle style) {
        row.createCell(column).setCellValue(value);
        row.getCell(column).setCellStyle(style);
    }

    private void createStyledCell(Row row, int column, int value, CellStyle style) {
        row.createCell(column).setCellValue(value);
        row.getCell(column).setCellStyle(style);
    }

    private int writeSheetMetadata(Sheet sheet, String facultyName, String courseName, CellStyle labelStyle, CellStyle valueStyle) {
        Row nameRow = sheet.createRow(0);
        createStyledCell(nameRow, 0, "Name of the Instructor:", labelStyle);
        createStyledCell(nameRow, 1, normalizeMetadataValue(facultyName), valueStyle);

        sheet.createRow(1);

        Row courseRow = sheet.createRow(2);
        createStyledCell(courseRow, 0, "Course name:", labelStyle);
        createStyledCell(courseRow, 1, normalizeMetadataValue(courseName), valueStyle);

        sheet.createRow(3);
        return 4;
    }

    private String normalizeMetadataValue(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim();
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}