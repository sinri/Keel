package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.poi.excel.KeelSheet;
import io.github.sinri.keel.poi.excel.KeelSheets;
import io.github.sinri.keel.poi.excel.entity.KeelSheetMatrix;
import io.github.sinri.keel.poi.excel.entity.KeelSheetMatrixRow;
import io.github.sinri.keel.poi.excel.entity.KeelSheetTemplatedMatrix;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.github.sinri.keel.tesuto.TestUnitResult;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

public class ReadTemplatedExcelTest extends KeelTest {
    private static final String file = "/Users/leqee/code/Keel/src/test/resources/excel/excel_1.xlsx";
    private static final String fileXls = "/Users/leqee/code/Keel/src/test/resources/excel/excel_4.xls";


    @Nonnull
    @Override
    protected Future<Void> starting() {
        return Future.succeededFuture();
    }

    @Nonnull
    @Override
    protected Future<Void> ending(List<TestUnitResult> testUnitResults) {
        return Future.succeededFuture();
    }

    @TestUnit(skip = true)
    public Future<Void> test1() {
        try (KeelSheets keelSheets = KeelSheets.factory(file)) {
            KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
            KeelSheetMatrix keelSheetMatrix = keelSheet.blockReadAllRowsToMatrix(1, 6, null);
            keelSheetMatrix.getRawRowList().forEach(row -> {
                this.getIssueRecorder().info(log -> log.message("BLOCK: " + KeelHelpers.stringHelper().joinStringArray(row, ", ")));
            });

            keelSheetMatrix.getRowIterator(KeelSheetMatrixRowExt.class).forEachRemaining(r -> {
                this.getIssueRecorder().info(log -> log.message("record")
                        .context(c -> c
                        .put("record_id", r.recordId())
                        .put("name", r.name())
                        .put("age", r.age())
                        )
                );
            });
        }
        return Future.succeededFuture();
    }

    @TestUnit(skip = false)
    public Future<Void> test2() {
        KeelSheets keelSheets = KeelSheets.factory(file);
        KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
        return keelSheet.readAllRowsToMatrix(1, 0, null)
                .compose(keelSheetMatrix -> {
                    keelSheetMatrix.getRawRowList().forEach(row -> {
                        this.getIssueRecorder().info(log -> log.message("ASYNC: " + KeelHelpers.stringHelper().joinStringArray(row, ", ")));
                    });
                    return Future.succeededFuture();
                })
                .andThen(ar -> {
                    keelSheets.close();
                })
                .compose(v -> {
                    return Future.succeededFuture();
                });
    }

    @TestUnit(skip = true)
    public Future<Void> test3() {
        try (KeelSheets keelSheets = KeelSheets.factory(file)) {
            KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
            KeelSheetTemplatedMatrix templatedMatrix = keelSheet.blockReadAllRowsToTemplatedMatrix(0, 6, null);
            templatedMatrix.getRows().forEach(row -> {
                this.getIssueRecorder().info(log -> log.message("BLOCK TEMPLATED: " + row.toJsonObject()));
            });
        }
        return Future.succeededFuture();
    }

    @TestUnit(skip = true)
    public Future<Void> test4() {
        KeelSheets keelSheets = KeelSheets.factory(file);
        KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
        return keelSheet.readAllRowsToTemplatedMatrix(0, 7, null)
                .compose(templatedMatrix -> {
                    templatedMatrix.getRows().forEach(row -> {
                        this.getIssueRecorder().info(log -> log.message("ASYNC TEMPLATED: " + row.toJsonObject()));
                    });

                    return Future.succeededFuture();
                })
                .andThen(ar -> {
                    keelSheets.close();
                })
                .compose(v -> {
                    return Future.succeededFuture();
                });

    }

    @TestUnit(skip = true)
    public Future<Void> test5() {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(new File(fileXls));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        KeelSheets keelSheets = KeelSheets.factory(fileInputStream);
        KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
        return keelSheet.readAllRowsToTemplatedMatrix(0, 128, null)
                .compose(templatedMatrix -> {
                    templatedMatrix.getRows().forEach(row -> {
                        this.getIssueRecorder().info(log -> log.message("ASYNC TEMPLATED: " + row.toJsonObject()));
                    });

                    return Future.succeededFuture();
                })
                .andThen(ar -> {
                    keelSheets.close();
                })
                .compose(v -> {
                    return Future.succeededFuture();
                });

    }

    public static class KeelSheetMatrixRowExt extends KeelSheetMatrixRow {

        public KeelSheetMatrixRowExt(List<String> rawRow) {
            super(rawRow);
        }

        public Integer recordId() {
            return readValueToInteger(0);
        }

        public String name() {
            return readValue(1);
        }

        public double age() {
            return readValueToDouble(2);
        }
    }
}
