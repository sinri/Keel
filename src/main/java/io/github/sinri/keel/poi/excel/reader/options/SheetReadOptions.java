package io.github.sinri.keel.poi.excel.reader.options;

import io.github.sinri.keel.core.ValueBox;
import io.vertx.core.Handler;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 3.2.1
 */
public class SheetReadOptions {
    /**
     * Load sheet with 3 kinds of cell formula evaluator: None, Cached, and Evaluate.
     */
    private final @NotNull ValueBox<FormulaEvaluator> formulaEvaluatorBox = new ValueBox<>();


    /**
     * Configure the read options for each column.
     * If one column is not configured,
     * the read code would follow defaultColumnReadOptions,
     * but the column name would be thrown as omitted.
     */
    private final Map<Integer, ColumnReadOptions> columnReadOptionsMap = new HashMap<>();
    private final ColumnReadOptions defaultColumnReadOptions = new ColumnReadOptions();

    /**
     * SET cell formula evaluator to Cached or Evaluate.
     *
     * @param formulaEvaluator Null as Cached, or Evaluate.
     */
    public SheetReadOptions setFormulaEvaluator(@Nullable FormulaEvaluator formulaEvaluator) {
        this.formulaEvaluatorBox.setValue(formulaEvaluator);
        return this;
    }

    /**
     * SET cell formula evaluator to None,
     */
    public SheetReadOptions removeFormulaEvaluator() {
        this.formulaEvaluatorBox.clear();
        return this;
    }

    @NotNull
    public ValueBox<FormulaEvaluator> getFormulaEvaluatorBox() {
        return formulaEvaluatorBox;
    }

    public ColumnReadOptions getDefaultColumnReadOptions() {
        return defaultColumnReadOptions;
    }

    public SheetReadOptions maintainDefaultColumnReadOptions(Handler<ColumnReadOptions> handler) {
        handler.handle(this.defaultColumnReadOptions);
        return this;
    }

    public Map<Integer, ColumnReadOptions> getColumnReadOptionsMap() {
        return columnReadOptionsMap;
    }

    public SheetReadOptions setColumnReadOptions(Integer index, ColumnReadOptions columnReadOptions) {
        this.columnReadOptionsMap.put(index, columnReadOptions);
        return this;
    }

    @Nullable
    public ColumnReadOptions getColumnReadOptions(Integer index) {
        return columnReadOptionsMap.get(index);
    }
}
