package io.github.sinri.keel.excel.entity;



import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @since 3.0.8
 */
public class SimpleTemplatedRow extends AbstractTemplatedRow<SimpleTemplatedColumnNameMapping> {
    public SimpleTemplatedRow(@Nonnull List<String> rowAsList, @Nullable SimpleTemplatedColumnNameMapping columnNameMapping) {
        super(rowAsList, columnNameMapping);
    }

    public SimpleTemplatedRow(@Nonnull List<String> rowAsList, @Nullable List<String> columnNames) {
        super(rowAsList, columnNames == null ? null : new SimpleTemplatedColumnNameMapping(columnNames));
    }

    public SimpleTemplatedRow(@Nonnull List<String> rowAsList, @Nonnull Map<String, Integer> columnNameMap) {
        super(rowAsList, new SimpleTemplatedColumnNameMapping(columnNameMap));
    }
}
