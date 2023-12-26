package io.github.sinri.keel.poi.excel.entity;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * It is designed to be overridden for customized row reader.
 *
 * @since 3.0.14
 * @since 3.0.18 Finished Technical Preview.
 */
abstract public class KeelSheetMatrixRow {
    private final List<String> rawRow;

    public KeelSheetMatrixRow(List<String> rawRow) {
        this.rawRow = rawRow;
    }

    @Nonnull
    public String readValue(int i) {
        return rawRow.get(i);
    }

    public int readValueToInteger(int i) {
        double v = readValueToDouble(i);
        return (int) v;
    }

    public long readValueToLong(int i) {
        double v = readValueToDouble(i);
        return (long) v;
    }

    public double readValueToDouble(int i) {
        return Double.parseDouble(readValue(i));
    }
}
