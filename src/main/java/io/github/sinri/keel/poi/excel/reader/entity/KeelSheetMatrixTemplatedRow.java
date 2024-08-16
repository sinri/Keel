package io.github.sinri.keel.poi.excel.reader.entity;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 */
public interface KeelSheetMatrixTemplatedRow {
    static KeelSheetMatrixTemplatedRow create(@NotNull KeelSheetMatrixRowTemplate template, @NotNull List<String> rawRow) {
        return new KeelSheetMatrixTemplatedRowImpl(template, rawRow);
    }

    KeelSheetMatrixRowTemplate getTemplate();

    String getColumnValue(int i);

    String getColumnValue(String name);

    List<String> getRawRow();

    JsonObject toJsonObject();
}
