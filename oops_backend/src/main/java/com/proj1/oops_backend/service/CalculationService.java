package com.proj1.oops_backend.service;

import com.proj1.oops_backend.model.CalculationResult;
import com.proj1.oops_backend.model.Report;
import com.proj1.oops_backend.model.WorkbookInput;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CalculationService {

    private static final List<String> ACADEMIC_SHEET_ORDER = List.of("it1", "it2", "it3", "sem");

    public CalculationResult calculate(WorkbookInput workbookInput) {
        List<String> markColumns = buildAcademicMarkColumns(workbookInput);
        List<String> coCodes = detectCoCodes(workbookInput);

        Map<String, Double> internalMaxByCo = new LinkedHashMap<>();
        Map<String, Double> semMaxByCo = new LinkedHashMap<>();
        initializeMaxMaps(coCodes, internalMaxByCo, semMaxByCo);
        populateMaxMarks(workbookInput, internalMaxByCo, semMaxByCo);

        Map<String, Double> internalThresholdByCo = new LinkedHashMap<>();
        Map<String, Double> semThresholdByCo = new LinkedHashMap<>();
        Map<String, Double> totalThresholdByCo = new LinkedHashMap<>();
        for (String co : coCodes) {
            double internalThreshold = internalMaxByCo.getOrDefault(co, 0.0) * 0.50;
            double semThreshold = semMaxByCo.getOrDefault(co, 0.0) * 0.45;
            internalThresholdByCo.put(co, internalThreshold);
            semThresholdByCo.put(co, semThreshold);
            totalThresholdByCo.put(co, internalThreshold + semThreshold);
        }

        List<CalculationResult.StudentPerformanceRow> studentRows = workbookInput.academicSheets().get("sem").students().stream()
                .map(student -> buildStudentRow(student, workbookInput, coCodes, markColumns, totalThresholdByCo))
                .toList();

        Map<String, CalculationResult.CesSummaryRow> cesSummaryByCo = computeCesSummary(workbookInput, coCodes);
        
        List<CalculationResult.CoSummaryRow> coSummaryRows = computeCoSummary(
                studentRows,
                coCodes,
                internalThresholdByCo,
                semThresholdByCo,
                totalThresholdByCo);

        List<CalculationResult.CesSummaryRow> cesSummaryRows = coCodes.stream()
                .map(co -> cesSummaryByCo.getOrDefault(co, new CalculationResult.CesSummaryRow(co, 0, 0, 0.0, 0)))
                .toList();

        List<CalculationResult.FinalAttainmentRow> finalRows = computeFinalAttainment(coSummaryRows, cesSummaryByCo);

        return new CalculationResult(markColumns, coCodes, studentRows, coSummaryRows, cesSummaryRows, finalRows);
    }

    public List<Report> toReports(CalculationResult result, String sourceFileName) {
        List<Report> reports = new java.util.ArrayList<>();
        int rowNumber = 1;
        for (CalculationResult.StudentPerformanceRow row : result.studentRows()) {
            String rawData = "roll_no=" + row.rollNo() + " | name=" + row.studName();
            String calculatedData = "coTotals=" + row.coTotals() + " | coAttainment=" + row.coAttainment();
            reports.add(new Report(sourceFileName, rowNumber, rawData, calculatedData));
            rowNumber++;
        }
        return reports;
    }

    private List<String> buildAcademicMarkColumns(WorkbookInput workbookInput) {
        List<String> columns = new java.util.ArrayList<>();
        for (String sheetName : ACADEMIC_SHEET_ORDER) {
            WorkbookInput.AcademicSheet sheet = workbookInput.academicSheets().get(sheetName);
            if (sheet == null) continue;

            for (int i = 0; i < sheet.questions().size(); i++) {
                columns.add(sheetName + "_q" + (i + 1) + "_mks");
            }
            columns.add(sheetName + "_tot_mks");
        }

        WorkbookInput.CesSheet cesSheet = workbookInput.cesSheet();
        for (int i = 0; i < cesSheet.questions().size(); i++) {
            columns.add("ces_q" + (i + 1));
        }
        return columns;
    }

    private List<String> detectCoCodes(WorkbookInput workbookInput) {
        Set<String> coSet = new LinkedHashSet<>();
        for (WorkbookInput.AcademicSheet sheet : workbookInput.academicSheets().values()) {
            for (WorkbookInput.Question question : sheet.questions()) {
                if (isValidCo(question.coCode())) {
                    coSet.add(question.coCode().trim());
                }
            }
        }
        for (WorkbookInput.CesQuestion question : workbookInput.cesSheet().questions()) {
            if (isValidCo(question.coCode())) {
                coSet.add(question.coCode().trim());
            }
        }

        return coSet.stream()
                .sorted(Comparator.comparing(String::toString))
                .toList();
    }

    private boolean isValidCo(String co) {
        if (co == null || co.isBlank()) return false;
        String check = co.trim().toUpperCase(Locale.ROOT);
        if (check.equals("MARKS") || check.equals("CO") || check.equals("CO_UNMAPPED")) return false;
        if (check.matches("^[0-9]+(\\.[0-9]+)?$")) return false; 
        return true;
    }

    private void initializeMaxMaps(List<String> coCodes, Map<String, Double> internalMaxByCo, Map<String, Double> semMaxByCo) {
        for (String co : coCodes) {
            internalMaxByCo.put(co, 0.0);
            semMaxByCo.put(co, 0.0);
        }
    }

    private void populateMaxMarks(WorkbookInput workbookInput, Map<String, Double> internalMaxByCo, Map<String, Double> semMaxByCo) {
        for (Map.Entry<String, WorkbookInput.AcademicSheet> entry : workbookInput.academicSheets().entrySet()) {
            String sheetName = entry.getKey();
            WorkbookInput.AcademicSheet sheet = entry.getValue();

            for (WorkbookInput.Question q : sheet.questions()) {
                if (!isValidCo(q.coCode())) continue;
                
                String co = q.coCode().trim();
                if ("sem".equalsIgnoreCase(sheetName)) {
                    semMaxByCo.computeIfPresent(co, (k, v) -> v + q.maxMarks());
                } else {
                    internalMaxByCo.computeIfPresent(co, (k, v) -> v + q.maxMarks());
                }
            }
        }
    }

    private CalculationResult.StudentPerformanceRow buildStudentRow(
            WorkbookInput.StudentMarks semStudent,
            WorkbookInput workbookInput,
            List<String> coCodes,
            List<String> markColumns,
            Map<String, Double> totalThresholdByCo) {
        String roll = semStudent.rollNo();
        String name = semStudent.name();

        Map<String, Double> marksByColumn = new LinkedHashMap<>();
        Map<String, Double> coTotals = new LinkedHashMap<>();
        Map<String, Double> internalCoTotals = new LinkedHashMap<>();
        Map<String, Double> semCoTotals = new LinkedHashMap<>();
        
        for (String co : coCodes) {
            coTotals.put(co, 0.0);
            internalCoTotals.put(co, 0.0);
            semCoTotals.put(co, 0.0);
        }

        for (String sheetName : ACADEMIC_SHEET_ORDER) {
            WorkbookInput.AcademicSheet sheet = workbookInput.academicSheets().get(sheetName);
            if (sheet == null) continue;

            WorkbookInput.StudentMarks student = findAcademicStudent(sheet.students(), roll, name);
            double total = 0.0;
            for (int i = 0; i < sheet.questions().size(); i++) {
                double mark = student == null || i >= student.marks().size() ? 0.0 : student.marks().get(i);
                marksByColumn.put(sheetName + "_q" + (i + 1) + "_mks", mark);
                total += mark;

                String rawCo = sheet.questions().get(i).coCode();
                if (isValidCo(rawCo)) {
                    String co = rawCo.trim();
                    coTotals.computeIfPresent(co, (k, v) -> v + mark);
                    if ("sem".equalsIgnoreCase(sheetName)) {
                        semCoTotals.computeIfPresent(co, (k, v) -> v + mark);
                    } else {
                        internalCoTotals.computeIfPresent(co, (k, v) -> v + mark);
                    }
                }
            }
            marksByColumn.put(sheetName + "_tot_mks", student == null ? total : student.totalMarks());
        }

        WorkbookInput.CesResponse cesResponse = findCesStudent(workbookInput.cesSheet().students(), roll, name);
        int cesCount = workbookInput.cesSheet().questions().size();
        for (int i = 0; i < cesCount; i++) {
            double encoded = encodeCesResponseAsNumeric(cesResponse, i);
            marksByColumn.put("ces_q" + (i + 1), encoded);
        }

        Map<String, Integer> coAttainment = new LinkedHashMap<>();
        for (String co : coCodes) {
            double obtained = coTotals.getOrDefault(co, 0.0);
            double threshold = totalThresholdByCo.getOrDefault(co, 0.0);
            coAttainment.put(co, obtained >= threshold && obtained > 0.0 ? 1 : 0);
        }

        Map<String, Double> orderedMarks = new LinkedHashMap<>();
        for (String col : markColumns) {
            orderedMarks.put(col, marksByColumn.getOrDefault(col, 0.0));
        }

        return new CalculationResult.StudentPerformanceRow(roll, name, orderedMarks, coTotals, coAttainment, internalCoTotals, semCoTotals);
    }

    private WorkbookInput.StudentMarks findAcademicStudent(List<WorkbookInput.StudentMarks> students, String roll, String name) {
        for (WorkbookInput.StudentMarks s : students) {
            if (!roll.isBlank() && !s.rollNo().isBlank() && roll.trim().equalsIgnoreCase(s.rollNo().trim())) {
                return s;
            } else if (!name.isBlank() && !s.name().isBlank() && name.trim().equalsIgnoreCase(s.name().trim())) {
                return s;
            }
        }
        return null;
    }

    private WorkbookInput.CesResponse findCesStudent(List<WorkbookInput.CesResponse> students, String roll, String name) {
        for (WorkbookInput.CesResponse s : students) {
            if (!roll.isBlank() && !s.rollNo().isBlank() && roll.trim().equalsIgnoreCase(s.rollNo().trim())) {
                return s;
            } else if (!name.isBlank() && !s.name().isBlank() && name.trim().equalsIgnoreCase(s.name().trim())) {
                return s;
            }
        }
        return null;
    }

    private double encodeCesResponseAsNumeric(WorkbookInput.CesResponse response, int index) {
        if (response == null || index >= response.responses().size()) return 0.0;
        String value = response.responses().get(index).trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "STRONGLY AGREE", "STRONGLY_AGREE" -> 4.0;
            case "AGREE" -> 3.0;
            case "NEUTRAL" -> 2.0;
            case "DISAGREE" -> 1.0;
            default -> 0.0;
        };
    }

    private List<CalculationResult.CoSummaryRow> computeCoSummary(
            List<CalculationResult.StudentPerformanceRow> studentRows,
            List<String> coCodes,
            Map<String, Double> internalThresholdByCo,
            Map<String, Double> semThresholdByCo,
            Map<String, Double> totalThresholdByCo) {
        
        List<CalculationResult.CoSummaryRow> summary = new java.util.ArrayList<>();

        for (String co : coCodes) {
            int attained = 0, attempted = 0;
            int internalAttained = 0, internalAttempted = 0;
            int semAttained = 0, semAttempted = 0;

            double internalThreshold = internalThresholdByCo.getOrDefault(co, 0.0);
            double semThreshold = semThresholdByCo.getOrDefault(co, 0.0);
            double totalThreshold = totalThresholdByCo.getOrDefault(co, 0.0);

            for (CalculationResult.StudentPerformanceRow row : studentRows) {
                double totalObtained = row.coTotals().getOrDefault(co, 0.0);
                double internalObtained = row.internalCoTotals().getOrDefault(co, 0.0);
                double semObtained = row.semCoTotals().getOrDefault(co, 0.0);

                if (totalObtained > 0.0) {
                    attempted++;
                    if (totalObtained >= totalThreshold) attained++;
                }

                if (internalObtained > 0.0) {
                    internalAttempted++;
                    if (internalObtained >= internalThreshold) internalAttained++;
                }

                if (semObtained > 0.0) {
                    semAttempted++;
                    if (semObtained >= semThreshold) semAttained++;
                }
            }

            double overallPercentage = percentage(attained, attempted);
            double internalPercentage = percentage(internalAttained, internalAttempted);
            double semPercentage = percentage(semAttained, semAttempted);

            summary.add(new CalculationResult.CoSummaryRow(
                    co, attained, attempted, overallPercentage, mapAcademicLevel(overallPercentage),
                    internalAttained, internalAttempted, internalPercentage, mapAcademicLevel(internalPercentage),
                    semAttained, semAttempted, semPercentage, mapAcademicLevel(semPercentage),
                    internalThreshold, semThreshold, totalThreshold));
        }

        return summary;
    }

    private Map<String, CalculationResult.CesSummaryRow> computeCesSummary(WorkbookInput workbookInput, List<String> coCodes) {
        Map<String, Integer> agreeCounts = new HashMap<>();
        Map<String, Integer> totals = new HashMap<>();
        for (String co : coCodes) {
            agreeCounts.put(co, 0);
            totals.put(co, 0);
        }

        List<WorkbookInput.CesQuestion> questions = workbookInput.cesSheet().questions();
        for (WorkbookInput.CesResponse student : workbookInput.cesSheet().students()) {
            for (int i = 0; i < questions.size(); i++) {
                String rawCo = questions.get(i).coCode();
                if (!isValidCo(rawCo)) continue;
                
                String co = rawCo.trim();
                String response = i < student.responses().size() ? student.responses().get(i) : "";
                if (response == null || response.isBlank()) continue;

                totals.computeIfPresent(co, (k, v) -> v + 1);
                String upper = response.trim().toUpperCase(Locale.ROOT);
                if ("AGREE".equals(upper) || "STRONGLY AGREE".equals(upper) || "STRONGLY_AGREE".equals(upper)) {
                    agreeCounts.computeIfPresent(co, (k, v) -> v + 1);
                }
            }
        }

        Map<String, CalculationResult.CesSummaryRow> result = new LinkedHashMap<>();
        for (String co : coCodes) {
            int agree = agreeCounts.getOrDefault(co, 0);
            int total = totals.getOrDefault(co, 0);
            double pct = percentage(agree, total);
            result.put(co, new CalculationResult.CesSummaryRow(co, agree, total, pct, mapCesLevel(pct)));
        }
        return result;
    }

    private List<CalculationResult.FinalAttainmentRow> computeFinalAttainment(
            List<CalculationResult.CoSummaryRow> coSummaryRows,
            Map<String, CalculationResult.CesSummaryRow> cesSummaryByCo) {
        return coSummaryRows.stream().map(co -> {
            CalculationResult.CesSummaryRow ces = cesSummaryByCo.getOrDefault(
                    co.coCode(),
                    new CalculationResult.CesSummaryRow(co.coCode(), 0, 0, 0.0, 0));

            double directAttainmentLevel = (co.internalLevel() * 0.5) + (co.semLevel() * 0.5);
            double finalAttainmentLevel = (0.8 * directAttainmentLevel) + (0.2 * ces.level());

            return new CalculationResult.FinalAttainmentRow(
                    co.coCode(),
                    co.internalPercentage(),
                    co.internalLevel(),
                    co.semPercentage(),
                    co.semLevel(),
                    directAttainmentLevel,
                    ces.percentage(),
                    ces.level(),
                    finalAttainmentLevel,
                    (int) Math.round(finalAttainmentLevel)); 
        }).toList();
    }

    private double percentage(int numerator, int denominator) {
        if (denominator == 0) return 0.0;
        return (numerator * 100.0) / denominator;
    }

    private int mapAcademicLevel(double percentage) {
        if (percentage >= 50.0) return 3;
        if (percentage >= 40.0) return 2;
        if (percentage >= 30.0) return 1;
        return 0;
    }

    private int mapCesLevel(double percentage) {
        if (percentage >= 80.0) return 3;
        if (percentage >= 65.0) return 2;
        if (percentage >= 40.0) return 1;
        return 0;
    }
}