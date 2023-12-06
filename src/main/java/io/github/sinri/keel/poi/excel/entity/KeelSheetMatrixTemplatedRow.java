package io.github.sinri.keel.poi.excel.entity;

import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.List;

@TechnicalPreview(since = "3.0.13")
public interface KeelSheetMatrixTemplatedRow {
    static KeelSheetMatrixTemplatedRow create(@Nonnull KeelSheetMatrixRowTemplate template, @Nonnull List<String> rawRow) {
        return new KeelSheetMatrixTemplatedRowImpl(template, rawRow);
    }

    KeelSheetMatrixRowTemplate getTemplate();

    String getColumnValue(int i);

    String getColumnValue(String name);

    List<String> getRawRow();

    JsonObject toJsonObject();
}
