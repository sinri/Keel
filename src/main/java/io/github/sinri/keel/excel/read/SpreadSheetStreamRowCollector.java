package io.github.sinri.keel.excel.read;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@TechnicalPreview(since = "3.0.12")
public class SpreadSheetStreamRowCollector implements ReadListener<Map<Integer, String>> {
    private final SpreadSheetStream stream;

    public SpreadSheetStreamRowCollector(Function<List<List<String>>, Future<Void>> processor, int batchSize) {
        this.stream = new SpreadSheetStream(processor, batchSize);
    }

    public SpreadSheetStream getStream() {
        return stream;
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        List<String> headerRow = new ArrayList<>();
        headMap.forEach((i, cell) -> {
            //System.out.println("SpreadSheetMatrixRowCollector::invokeHead -> " + cell.getData() + " as " + cell.getType());
            CellDataTypeEnum type = cell.getType();
            String x;
            if (type == CellDataTypeEnum.NUMBER) {
                x = String.valueOf(cell.getNumberValue());
            } else if (type == CellDataTypeEnum.BOOLEAN) {
                x = String.valueOf(cell.getBooleanValue());
            } else {
                x = cell.getStringValue();
            }
            headerRow.add(x);
        });
        this.getStream().addHeaderRow(headerRow);
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
//        System.out.println("invoke");
        List<String> rawRow = new ArrayList<>();
        // 这里按照测试结果，是有序的map
        data.forEach((k, v) -> rawRow.add(v));
        this.stream.handleRow(rawRow);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // do nothing
        System.out.println("doAfterAllAnalysed");
    }
}
