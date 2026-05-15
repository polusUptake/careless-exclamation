package com.proj1.oops_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceFileName;

    @Column(nullable = false)
    private Integer rowNumber;

    @Column(columnDefinition = "TEXT")
    private String rawData;

    @Column(columnDefinition = "TEXT")
    private String calculatedData;

    public Report() {
    }

    public Report(String sourceFileName, Integer rowNumber, String rawData, String calculatedData) {
        this.sourceFileName = sourceFileName;
        this.rowNumber = rowNumber;
        this.rawData = rawData;
        this.calculatedData = calculatedData;
    }

    public Long getId() {
        return id;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public String getCalculatedData() {
        return calculatedData;
    }

    public void setCalculatedData(String calculatedData) {
        this.calculatedData = calculatedData;
    }
}
