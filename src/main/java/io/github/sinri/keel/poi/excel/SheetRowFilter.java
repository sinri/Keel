package io.github.sinri.keel.poi.excel;

import java.util.List;

public interface SheetRowFilter {
    static SheetRowFilter toThrowEmptyRows() {
        return rawRow -> {
            boolean allEmpty = true;
            for (String cell : rawRow) {
                if (cell != null && !cell.isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
            return allEmpty;
        };
    }

    boolean shouldThrowThisRawRow(List<String> rawRow);


}
