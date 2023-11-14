package io.github.sinri.keel.excel.read;

import io.github.sinri.keel.excel.entity.TemplatedRow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @since 3.0.8
 */
public class SpreadSheetMatrix {
    private final List<List<String>> headers;
    private final List<List<String>> rows;

    public SpreadSheetMatrix() {
        this.headers = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public SpreadSheetMatrix addHeaderRow(List<String> headerRow) {
        this.headers.add(headerRow);
        return this;
    }

    public SpreadSheetMatrix addRow(List<String> row) {
        this.rows.add(row);
        return this;
    }

    public SpreadSheetMatrix addRows(List<List<String>> rows) {
        this.rows.addAll(rows);
        return this;
    }

    public List<List<String>> getHeaders() {
        return headers;
    }

    public List<String> getRawRow(int i) {
        return rows.get(i);
    }

    public <R extends TemplatedRow> R getTemplatedRow(int i, Function<List<String>, R> rowTransformer) {
        List<String> rawRow = getRawRow(i);
        return rowTransformer.apply(rawRow);
    }

    public List<List<String>> getRawRowList() {
        return rows;
    }

    public <R extends TemplatedRow> List<R> getTemplatedRowList(Function<List<String>, R> rowTransformer) {
        List<R> list = new ArrayList<>();
        getRawRowList().forEach(rawRow -> {
            R r = rowTransformer.apply(rawRow);
            list.add(r);
        });
        return list;
    }
}
