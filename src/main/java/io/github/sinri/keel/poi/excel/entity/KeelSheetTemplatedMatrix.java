package io.github.sinri.keel.poi.excel.entity;

import javax.annotation.Nonnull;
import java.util.List;

public interface KeelSheetTemplatedMatrix {
    static KeelSheetTemplatedMatrix create(@Nonnull KeelSheetMatrixRowTemplate template) {
        return new KeelSheetTemplatedMatrixImpl(template);
    }

    KeelSheetMatrixRowTemplate getTemplate();

    KeelSheetMatrixTemplatedRow getRow(int index);

    List<KeelSheetMatrixTemplatedRow> getRows();

    KeelSheetTemplatedMatrix addRawRow(List<String> rawRow);

    default KeelSheetTemplatedMatrix addRawRows(List<List<String>> rawRows) {
        rawRows.forEach(this::addRawRow);
        return this;
    }
}
