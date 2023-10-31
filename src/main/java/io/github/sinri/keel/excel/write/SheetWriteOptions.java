package io.github.sinri.keel.excel.write;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.0.8
 */
public class SheetWriteOptions {
    private final Integer sheetNo;
    private final String sheetName;

    private final List<List<String>> headers;

    private SheetWriteOptions(Integer sheetNo, String sheetName) {
        this.sheetNo = sheetNo;
        this.sheetName = sheetName;
        this.headers = new ArrayList<>();
    }

    public static SheetWriteOptions forName(@Nonnull String name) {
        return new SheetWriteOptions(null, name);
    }

    public static SheetWriteOptions forIndex(int index) {
        if (index < 0) throw new IllegalArgumentException();
        return new SheetWriteOptions(index, null);
    }

    public Integer getSheetNo() {
        return sheetNo;
    }

    public String getSheetName() {
        return sheetName;
    }

    public List<List<String>> getHeaders() {
        return headers;
    }

    public SheetWriteOptions addHeaderRow(List<String> headerRow) {
        this.headers.add(headerRow);
        return this;
    }
}
