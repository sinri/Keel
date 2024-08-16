package io.github.sinri.keel.poi.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.jetbrains.annotations.NotNull;


/**
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 * @since 3.3.0 become abstract
 */
public abstract class KeelSheet {
    private final Sheet sheet;

    /**
     * @since 3.3.0
     */
    public KeelSheet(@NotNull Sheet sheet) {
        this.sheet = sheet;
    }

    /**
     * @return Raw Apache POI Sheet instance.
     */
    public Sheet getSheet() {
        return sheet;
    }

    public String getName() {
        return getSheet().getSheetName();
    }
}
