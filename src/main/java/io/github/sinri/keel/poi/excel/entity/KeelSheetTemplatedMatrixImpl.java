package io.github.sinri.keel.poi.excel.entity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class KeelSheetTemplatedMatrixImpl implements KeelSheetTemplatedMatrix {
    private final KeelSheetMatrixRowTemplate template;
    private final List<List<String>> rawRows;
    //private final List<KeelSheetMatrixTemplatedRow> templatedRows;

    KeelSheetTemplatedMatrixImpl(@Nonnull KeelSheetMatrixRowTemplate template) {
        this.template = template;
        this.rawRows = new ArrayList<>();
//        this.templatedRows = new ArrayList<>();
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
    public KeelSheetTemplatedMatrix addRawRow(List<String> rawRow) {
        this.rawRows.add(rawRow);
        //this.templatedRows.add(KeelSheetMatrixTemplatedRow.create(getTemplate(), rawRow));
        return this;
    }
}
