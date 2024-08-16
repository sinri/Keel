package io.github.sinri.keel.poi.excel.reader.entity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 */
public class KeelSheetTemplatedMatrixImpl implements KeelSheetTemplatedMatrix {
    private final KeelSheetMatrixRowTemplate template;
    private final List<List<String>> rawRows;

    KeelSheetTemplatedMatrixImpl(@NotNull KeelSheetMatrixRowTemplate template) {
        this.template = template;
        this.rawRows = new ArrayList<>();
    }

    @Override
    public List<List<String>> getRawRows() {
        return rawRows;
    }

    @Override
    public KeelSheetMatrixRowTemplate getTemplate() {
        return template;
    }

    @Override
    public KeelSheetMatrixTemplatedRow getRow(int index) {
        return KeelSheetMatrixTemplatedRow.create(getTemplate(), this.rawRows.get(index));
//        return this.templatedRows.get(index);
    }

    @Override
    public List<KeelSheetMatrixTemplatedRow> getRows() {
        List<KeelSheetMatrixTemplatedRow> templatedRows = new ArrayList<>();
        this.rawRows.forEach(rawRow -> {
            templatedRows.add(KeelSheetMatrixTemplatedRow.create(getTemplate(), rawRow));
        });
        return templatedRows;
    }

    @Override
    public KeelSheetTemplatedMatrix addRawRow(@NotNull List<String> rawRow) {
        this.rawRows.add(rawRow);
        return this;
    }
}
