package io.github.sinri.keel.excel.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @since 3.0.8
 */
public interface TemplatedColumnNameMapping {


    List<String> getColumnNameList();

    Map<String, Integer> getColumnNameMap();

    default String getColumnNameForIndex(int columnIndex) {
        return this.getColumnNameList().get(columnIndex);
    }

    default @Nullable Integer getColumnIndexForName(@Nonnull String columnName) {
        return this.getColumnNameMap().get(columnName);
    }
}
