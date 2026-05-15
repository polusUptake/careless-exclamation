package com.proj1.oops_backend.model;

import java.util.List;
import java.util.Map;

public record WorkbookInput(
        Map<String, AcademicSheet> academicSheets,
        List<String> academicSheetOrder,
        CesSheet cesSheet) {

    public record AcademicSheet(
            String sheetName,
            List<Question> questions,
            List<StudentMarks> students) {
    }

    public record Question(
            String questionId,
            String coCode,
            double maxMarks) {
    }

    public record StudentMarks(
            String rollNo,
            String name,
            List<Double> marks,
            double totalMarks) {
    }

    public record CesSheet(
            String sheetName,
            List<CesQuestion> questions,
            List<CesResponse> students) {
    }

    public record CesQuestion(
            String questionId,
            String coCode) {
    }

    public record CesResponse(
            String rollNo,
            String name,
            List<String> responses) {
    }
}