package io.github.sinri.keel.poi.excel.entity;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 */
public interface KeelSheetTemplatedMatrix {
    static KeelSheetTemplatedMatrix create(@Nonnull KeelSheetMatrixRowTemplate template) {
        return new KeelSheetTemplatedMatrixImpl(template);
    }

    KeelSheetMatrixRowTemplate getTemplate();

    KeelSheetMatrixTemplatedRow getRow(int index);

    List<KeelSheetMatrixTemplatedRow> getRows();

    List<List<String>> getRawRows();

    KeelSheetTemplatedMatrix addRawRow(@Nonnull List<String> rawRow);

    default KeelSheetTemplatedMatrix addRawRows(@Nonnull List<List<String>> rawRows) {
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
