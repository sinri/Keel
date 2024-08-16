package io.github.sinri.keel.poi.excel.reader.entity;


import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 */
public interface KeelSheetTemplatedMatrix {
    static KeelSheetTemplatedMatrix create(@NotNull KeelSheetMatrixRowTemplate template) {
        return new KeelSheetTemplatedMatrixImpl(template);
    }

    KeelSheetMatrixRowTemplate getTemplate();

    KeelSheetMatrixTemplatedRow getRow(int index);

    List<KeelSheetMatrixTemplatedRow> getRows();

    List<List<String>> getRawRows();

    KeelSheetTemplatedMatrix addRawRow(@NotNull List<String> rawRow);

    default KeelSheetTemplatedMatrix addRawRows(@NotNull List<List<String>> rawRows) {
        rawRows.forEach(this::addRawRow);
        return this;
    }

    default KeelSheetMatrix transformToMatrix() {
        KeelSheetMatrix keelSheetMatrix = new KeelSheetMatrix();
        keelSheetMatrix.setHeaderRow(getTemplate().getColumnNames());
        keelSheetMatrix.addRows(getRawRows());
        return keelSheetMatrix;
    }

}
