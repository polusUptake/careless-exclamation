package com.proj1.oops_backend.model;

import java.util.List;
import java.util.Map;

public record CalculationResult(
        List<String> academicMarkColumns,
        List<String> coCodes,
        List<StudentPerformanceRow> studentRows,
        List<CoSummaryRow> coSummaryRows,
        List<CesSummaryRow> cesSummaryRows,
        List<FinalAttainmentRow> finalAttainmentRows) {

    public record StudentPerformanceRow(
            String rollNo,
            String studName,
            Map<String, Double> marksByColumn,
            Map<String, Double> coTotals,
            Map<String, Integer> coAttainment,
            Map<String, Double> internalCoTotals,
            Map<String, Double> semCoTotals) {
    }

    public record CoSummaryRow(
            String coCode,
            int studentsAttained,
            int studentsAttempted,
            double percentage,
            int level,
            int internalAttained,
            int internalAttempted,
            double internalPercentage,
            int internalLevel,
            int semAttained,
            int semAttempted,
            double semPercentage,
            int semLevel,
            double internalThreshold,
            double semThreshold,
            double totalThreshold) {
    }

    public record CesSummaryRow(
            String coCode,
            int agreeAndStronglyAgree,
            int totalStudents,
            double percentage,
            int level) {
    }

    public record FinalAttainmentRow(
            String coCode,
            double internalPercentage,
            int internalLevel,
            double semPercentage,
            int semLevel,
            double directAttainment,
            double cesPercentage,
            int cesLevel,
            double finalAttainment,
            int finalLevel) {
    }
}