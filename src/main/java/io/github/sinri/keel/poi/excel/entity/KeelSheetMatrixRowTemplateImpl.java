package io.github.sinri.keel.poi.excel.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 */
public class KeelSheetMatrixRowTemplateImpl implements KeelSheetMatrixRowTemplate {
    private final List<String> headerRow;
    private final Map<String, Integer> headerMap;

    KeelSheetMatrixRowTemplateImpl(@Nonnull List<String> headerRow) {
        this.headerRow = headerRow;
        this.headerMap = new LinkedHashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            this.headerMap.put(Objects.requireNonNullElse(headerRow.get(i), ""), i);
        }
    }

    @Nonnull
    @Override
    public String getColumnName(int i) {
        return this.headerRow.get(i);
    }

    @Nullable
    @Override
    public Integer getColumnIndex(String name) {
        return this.headerMap.get(name);
    }

    @Nonnull
    @Override
    public List<String> getColumnNames() {
        return headerRow;
    }
}
