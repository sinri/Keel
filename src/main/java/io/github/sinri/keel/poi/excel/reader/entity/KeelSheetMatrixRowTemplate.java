package io.github.sinri.keel.poi.excel.reader.entity;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The columns definition of rows in a templated matrix.
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 */
public interface KeelSheetMatrixRowTemplate {
    static KeelSheetMatrixRowTemplate create(@NotNull List<String> headerRow) {
        return new KeelSheetMatrixRowTemplateImpl(headerRow);
    }

    /**
     * @param i Column index start from 0.
     * @return Column name at index.
     * @throws RuntimeException if index is out of bound
     */
    @NotNull
    String getColumnName(int i);

    /**
     * @param name the column name to seek.
     * @return The first met (or by customized logic) index of the given column name, and null if not found.
     */
    @Nullable
    Integer getColumnIndex(String name);

    @NotNull
    List<String> getColumnNames();
}
