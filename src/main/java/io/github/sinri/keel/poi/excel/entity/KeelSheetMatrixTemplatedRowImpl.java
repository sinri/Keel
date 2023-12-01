package io.github.sinri.keel.poi.excel.entity;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class KeelSheetMatrixTemplatedRowImpl implements KeelSheetMatrixTemplatedRow {
    private final KeelSheetMatrixRowTemplate template;
    private final List<String> rawRow;

    KeelSheetMatrixTemplatedRowImpl(@Nonnull KeelSheetMatrixRowTemplate template, @Nonnull List<String> rawRow) {
        this.template = template;
        this.rawRow = rawRow;
    }

    @Override
    public KeelSheetMatrixRowTemplate getTemplate() {
        return template;
    }

    @Override
    public String getColumnValue(int i) {
        return this.rawRow.get(i);
    }

    @Override
    public String getColumnValue(String name) {
        Integer columnIndex = getTemplate().getColumnIndex(name);
        return this.rawRow.get(Objects.requireNonNull(columnIndex));
    }

    @Override
    public JsonObject toJsonObject() {
        var x = new JsonObject();
        List<String> columnNames = this.template.getColumnNames();
        for (int i = 0; i < columnNames.size(); i++) {
            x.put(columnNames.get(i), getColumnValue(i));
        }
        return x;
    }
}
