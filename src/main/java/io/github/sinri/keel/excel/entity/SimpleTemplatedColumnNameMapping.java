package io.github.sinri.keel.excel.entity;


import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * @since 3.0.8
 */
public class SimpleTemplatedColumnNameMapping extends AbstractTemplatedColumnNameMapping {

    public SimpleTemplatedColumnNameMapping(@Nonnull List<String> columnNameList) {
        super(columnNameList);
    }

    public SimpleTemplatedColumnNameMapping(@Nonnull Map<String, Integer> columnNameMap) {
        super(columnNameMap);
    }
}
