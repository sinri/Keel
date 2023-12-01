package io.github.sinri.keel.poi.excel.entity;

import io.github.sinri.keel.core.TechnicalPreview;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * The columns definition of rows in a templated matrix.
 *
 */
@TechnicalPreview(since = "3.0.13")
public interface KeelSheetMatrixRowTemplate {
    static KeelSheetMatrixRowTemplate create(@Nonnull List<String> headerRow) {
        return new KeelSheetMatrixRowTemplateImpl(headerRow);
    }

    /**
     * @param i Column index start from 0.
     * @return Column name at index.
     * @throws RuntimeException if index is out of bound
     */
    @Nonnull
    String getColumnName(int i);

    /**
     * @param name the column name to seek.
     * @return The first met (or by customized logic) index of the given column name, and null if not found.
     */
    @Nullable
    Integer getColumnIndex(String name);

    @Nonnull
    List<String> getColumnNames();
}
