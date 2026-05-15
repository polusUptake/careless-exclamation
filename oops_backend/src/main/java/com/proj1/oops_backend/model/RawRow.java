package com.proj1.oops_backend.model;

import java.util.List;

public class RawRow {

    private final int rowNumber;
    private final List<String> cells;

    public RawRow(int rowNumber, List<String> cells) {
        this.rowNumber = rowNumber;
        this.cells = cells;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public List<String> getCells() {
        return cells;
    }
}
