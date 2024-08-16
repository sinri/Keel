package io.github.sinri.keel.poi.excel.reader.entity;

import io.github.sinri.keel.poi.excel.reader.options.ColumnReadOptions;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

/**
 * It is designed to be overridden for customized row reader.
 *
 * @since 3.0.14
 * @since 3.0.18 Finished Technical Preview.
 * @since 3.1.1 Use BigDecimal to handle the number value of cells.
 */
public class KeelSheetMatrixRow {
    private final List<String> rawRow;

    public KeelSheetMatrixRow(List<String> rawRow) {
        this.rawRow = rawRow;
    }

    @NotNull
    public String readValue(int i) {
        return rawRow.get(i);
    }

    /**
     * @since 3.1.1
     */
    public BigDecimal readValueToBigDecimal(int i) {
        return new BigDecimal(readValue(i));
    }

    /**
     * @since 3.1.1
     */
    public BigDecimal readValueToBigDecimalStrippedTrailingZeros(int i) {
        return new BigDecimal(readValue(i)).stripTrailingZeros();
    }

    @Nullable
    public Integer readValueToInteger(int i) {
        try {
            return readValueToBigDecimal(i).intValueExact();
        } catch (ArithmeticException arithmeticException) {
            return null;
        }
    }

    @Nullable
    public Long readValueToLong(int i) {
        try {
            return readValueToBigDecimal(i).longValueExact();
        } catch (ArithmeticException arithmeticException) {
            return null;
        }
    }

    public double readValueToDouble(int i) {
        return readValueToBigDecimal(i).doubleValue();
    }

    /**
     * @param columns definitions of columns as list
     * @return a json object with each column in defined type
     * @since 3.2.16
     */
    public JsonObject toJsonObject(@NotNull List<ColumnReadOptions> columns) {
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < columns.size(); i++) {
            ColumnReadOptions column = columns.get(i);
            Object value;
            switch (column.getColumnType()) {
                case Long:
                    value = readValueToLong(i);
                    break;
                case Double:
                    value = readValueToDouble(i);
                    break;
                case Integer:
                    value = readValueToInteger(i);
                    break;
                case BigDecimal:
                    value = readValueToBigDecimal(i);
                    break;
                case String:
                default:
                    value = readValue(i);
                    break;
            }
            jsonObject.put(column.getColumnName(), value);
        }
        return jsonObject;
    }

    /**
     * @since 3.2.16
     * Use Jackson to map Json Object to a java class.
     * @see <a href="https://github.com/FasterXML/jackson-databind">Jackson Databind</a>
     */
    public <T> T toBoundDataEntity(@NotNull List<ColumnReadOptions> columns, Class<T> tClass) {
        return toJsonObject(columns).mapTo(tClass);
    }
}
