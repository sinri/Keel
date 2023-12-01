package io.github.sinri.keel.poi.excel.entity;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.List;

public interface KeelSheetMatrixTemplatedRow {
    static KeelSheetMatrixTemplatedRow create(@Nonnull KeelSheetMatrixRowTemplate template, @Nonnull List<String> rawRow) {
        return new KeelSheetMatrixTemplatedRowImpl(template, rawRow);
    }

    KeelSheetMatrixRowTemplate getTemplate();

    String getColumnValue(int i);

    String getColumnValue(String name);

    JsonObject toJsonObject();
}
