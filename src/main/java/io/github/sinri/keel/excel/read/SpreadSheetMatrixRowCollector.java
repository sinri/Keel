package io.github.sinri.keel.excel.read;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since 3.0.8
 */
public class SpreadSheetMatrixRowCollector implements ReadListener<Map<Integer, String>> {

    private final SpreadSheetMatrix matrix = new SpreadSheetMatrix();

    public SpreadSheetMatrixRowCollector() {

    }

    public SpreadSheetMatrix getMatrix() {
        return matrix;
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        List<String> rawRow = new ArrayList<>();
        // 这里按照测试结果，是有序的map
        data.forEach((k, v) -> rawRow.add(v));
        this.matrix.addRow(rawRow);
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        //ReadListener.super.invokeHead(headMap, context);
        List<String> headerRow = new ArrayList<>();
        headMap.forEach((i, cell) -> {
            String x = cell.getStringValue();
            headerRow.add(x);
        });
        this.getMatrix().addHeaderRow(headerRow);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // do nothing
    }

    protected void handle(List<Map<Integer, String>> rows) {
        rows.forEach(row -> {
            List<String> rowAsList = new ArrayList<>();
            row.forEach((k, v) -> rowAsList.add(v));
            matrix.addRow(rowAsList);
        });
    }
}
