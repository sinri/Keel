package io.github.sinri.keel.poi.excel.entity;

import io.github.sinri.keel.core.TechnicalPreview;

import java.util.ArrayList;
import java.util.List;

@TechnicalPreview(since = "3.0.13")
public class KeelSheetMatrix {
    private final List<String> headerRow;
    private final List<List<String>> rows;

    public KeelSheetMatrix() {
        this.headerRow = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public KeelSheetMatrix addRow(List<String> row) {
        this.rows.add(row);
        return this;
    }

    public KeelSheetMatrix addRows(List<List<String>> rows) {
        this.rows.addAll(rows);
        return this;
    }

    public List<String> getHeaderRow() {
        return headerRow;
    }

    public KeelSheetMatrix setHeaderRow(List<String> headerRow) {
        this.headerRow.clear();
        this.headerRow.addAll(headerRow);
        return this;
    }

    public List<String> getRawRow(int i) {
        return this.rows.get(i);
    }

    public List<List<String>> getRawRowList() {
        return rows;
    }
}
