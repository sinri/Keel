package io.github.sinri.keel.poi.csv;

import io.github.sinri.keel.core.TechnicalPreview;

import javax.annotation.Nullable;
import java.math.BigDecimal;

/**
 * @since 3.1.1 Technical Preview
 */
@TechnicalPreview(since = "3.1.1")
public class CsvCell {
    private final @Nullable BigDecimal number;
    private final @Nullable String string;

    public CsvCell(@Nullable String s) {
        this.string = s;

        BigDecimal number1;
        if (s == null) number1 = null;
        else {
            try {
                number1 = new BigDecimal(s);
            } catch (NumberFormatException numberFormatException) {
                number1 = null;
            }
        }
        this.number = number1;
    }

    public boolean isNumber() {
        return this.number != null;
    }

    public boolean isEmpty() {
        return this.string != null && this.string.isEmpty();
    }

    public boolean isNull() {
        return this.string == null;
    }

    @Nullable
    public BigDecimal getNumber() {
        return number;
    }

    @Nullable
    public String getString() {
        return string;
    }
}
