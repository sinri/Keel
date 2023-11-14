package io.github.sinri.keel.excel.read;

import javax.annotation.Nonnull;

public class SheetReadOptions {
    private final Integer sheetNo;
    private final String sheetName;
    private int headRowNumber = 1;

    private SheetReadOptions(Integer sheetNo, String sheetName) {
        this.sheetName = sheetName;
        this.sheetNo = sheetNo;
    }

    public static SheetReadOptions forName(@Nonnull String name) {
        return new SheetReadOptions(null, name);
    }

    public static SheetReadOptions forIndex(int index) {
        if (index < 0) throw new IllegalArgumentException();
        return new SheetReadOptions(index, null);
    }

    public Integer getSheetNo() {
        return sheetNo;
    }

    public String getSheetName() {
        return sheetName;
    }

    public int getHeadRowNumber() {
        return headRowNumber;
    }

    public SheetReadOptions setHeadRowNumber(int headRowNumber) {
        this.headRowNumber = headRowNumber;
        return this;
    }
}
