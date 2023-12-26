package io.github.sinri.keel.mysql.dev;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @since 3.0.15
 * @since 3.0.18 Finished Technical Preview.
 */
public class TableRowClassFieldStrictEnum {
    private final String fieldName;
    private final String enumPackage;
    private final String enumClass;
    private final String enumClassRef;

    public TableRowClassFieldStrictEnum(@Nonnull String fieldName, @Nullable String enumPackage, @Nonnull String enumClass) {
        this.fieldName = fieldName;
        this.enumPackage = Objects.requireNonNullElse(enumPackage, "");
        this.enumClass = enumClass;

        enumClassRef = this.enumPackage + "." + enumClass;
        try {
            Class<?> enumClassToCheck = Class.forName(enumClassRef);
            if (!enumClassToCheck.isEnum()) {
                throw new RuntimeException("Defined Enum Class not enum in Strict Mode: " + enumClassRef);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Defined Enum Class not found in Strict Mode: " + enumClassRef, e);
        }
    }

    public String fullEnumRef() {
        return this.enumClassRef;
    }
}
