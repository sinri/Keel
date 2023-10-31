package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.excel.entity.SimpleTemplatedRow;
import io.github.sinri.keel.excel.read.KeelSpreadSheetReader;
import io.github.sinri.keel.excel.read.SheetReadOptions;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import java.util.List;
import java.util.Objects;

public class ReadExcelTest extends KeelVerticleBase {
    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions())
                .onSuccess(v -> {
                    new ReadExcelTest().deployMe(new DeploymentOptions())
                            .onSuccess(deployment_id -> {
                                System.out.println("deploy: " + deployment_id);
                            });
                });
    }

    @Override
    public void start() throws Exception {
        super.start();

        Future.succeededFuture()
                .compose(v -> {
                    return new KeelSpreadSheetReader("/Users/leqee/code/Keel/src/test/resources/excel/excel_1.xlsx")
                            .readEntireSheet(SheetReadOptions.forIndex(0));
                })
                .compose(spreadSheetMatrix -> {
                    spreadSheetMatrix.getHeaders().forEach(headerRow -> {
                        System.out.println("HEADER | " + KeelHelpers.stringHelper().joinStringArray(headerRow, " = "));
                    });

//                    spreadSheetMatrix.getRawRowList().forEach(rawRow -> {
//                        System.out.println("Raw Row: " + String.join(",", rawRow));
//                    });

//                    List<String> columnNames = List.of("record_id", "name", "age", "sex", "country", "level");
//                    SimpleTemplatedColumnNameMapping simpleTemplatedColumnNameMapping = new SimpleTemplatedColumnNameMapping(columnNames);
//                    spreadSheetMatrix.getTemplatedRowList(rawRow -> {
//                        return new SimpleTemplatedRow(rawRow, simpleTemplatedColumnNameMapping);
//                    }).forEach(sampleTemplatedRow -> {
//                        System.out.println("Row: " + sampleTemplatedRow.toJsonObject());
//                    });

                    spreadSheetMatrix.getTemplatedRowList(rawRow -> {
                        return new Record(rawRow);
                    }).forEach(sampleTemplatedRow -> {
                        System.out.println("Row | " + sampleTemplatedRow.recordId() + " | " + sampleTemplatedRow.name() + " | " + sampleTemplatedRow.toJsonObject());
                    });

                    return Future.succeededFuture();
                })
                .onFailure(throwable -> {
                    throwable.printStackTrace();
                })
                .eventually(v -> {
                    return Keel.getVertx().close();
                });
    }

    private static class Record extends SimpleTemplatedRow {
        private static final List<String> columnNames = List.of("record_id", "name", "age", "sex", "country", "level");

        public Record(List<String> rawRow) {
            super(rawRow, columnNames);
        }

        public Long recordId() {
            return Long.parseLong(Objects.requireNonNull(readColumn(0)));
        }

        public String name() {
            return readColumn(1);
        }
    }
}
