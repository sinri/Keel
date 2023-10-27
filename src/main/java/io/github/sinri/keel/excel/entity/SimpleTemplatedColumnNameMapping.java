package io.github.sinri.keel.excel.entity;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * @since 3.0.8
 */
public class SimpleTemplatedColumnNameMapping extends AbstractTemplatedColumnNameMapping {

    public SimpleTemplatedColumnNameMapping(@NotNull List<String> columnNameList) {
        super(columnNameList);
    }

    public SimpleTemplatedColumnNameMapping(@NotNull Map<String, Integer> columnNameMap) {
        super(columnNameMap);
    }
}
