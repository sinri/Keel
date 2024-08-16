package io.github.sinri.keel.test.lab.excel;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.poi.excel.FileAccessOptions;
import io.github.sinri.keel.poi.excel.KeelSheets;
import io.github.sinri.keel.poi.excel.reader.KeelSheetReader;
import io.github.sinri.keel.poi.excel.reader.options.ColumnReadOptions;
import io.github.sinri.keel.poi.excel.reader.options.ColumnType;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.List;

public class ColumnedTest extends KeelTest {
    private static final String fileName = "/Users/sinri/code/keel/src/test/resources/runtime/t1.xlsx";

    @TestUnit
    public Future<Void> test1() {
        KeelSheets keelSheets = KeelSheets.loadToRead(new FileAccessOptions()
                .setFile(fileName)
                .setWithFormulaEvaluator(true)
        );

        KeelSheetReader sheet1 = keelSheets.generateReaderForSheet(
                "Sheet1",
                readOptions -> readOptions
                        .maintainDefaultColumnReadOptions(c -> c
                                .setFormatDateTime(true)
                        )
        );
        List<ColumnReadOptions> columns = List.of(
                //姓名	科目	金额	核算数量	序列数值	校验
                ColumnReadOptions.build("姓名", ColumnType.String),
                ColumnReadOptions.build("科目", ColumnType.String),
                ColumnReadOptions.build("金额", ColumnType.Double),
                ColumnReadOptions.build("核算数量", ColumnType.Integer),
                ColumnReadOptions.build("序列数值", ColumnType.Long),
                ColumnReadOptions.build("校验", ColumnType.Double),
                ColumnReadOptions.build("确认日期", ColumnType.String),
                ColumnReadOptions.build("ddl", ColumnType.String),
                ColumnReadOptions.build("迷之数字", ColumnType.BigDecimal)
        );
        return sheet1.readAllRowsToMatrix().compose(matrix -> {
            return KeelAsyncKit.iterativelyCall(matrix.getRowIterator(), row -> {
                JsonObject jsonObject = row.toJsonObject(columns);
                getLogger().notice("Row", jsonObject);

//                DatumRow datumRow = jsonObject.mapTo(DatumRow.class);
                DatumRow datumRow = row.toBoundDataEntity(columns, DatumRow.class);
                getLogger().notice("Datum: " + datumRow.toString());

                return Future.succeededFuture();
            });
        });
    }


    public static class DatumRow {
        // {"姓名":"李四","科目":"炸鸡翅","金额":4.08,"核算数量":34,"序列数值":782649864981723498,"校验":0.12}
        private String name;
        private String category;
        private double amount;
        private int quantity;
        private long serialNumber;
        private double checksum;
        private String confirmDate;
        private String ddl;
        private BigDecimal nazo;

        @JsonGetter(value = "姓名")
        public String getName() {
            return name;
        }

        @JsonSetter(value = "姓名")
        public void setName(String name) {
            this.name = name;
        }

        @JsonGetter(value = "科目")
        public String getCategory() {
            return category;
        }

        @JsonSetter(value = "科目")
        public void setCategory(String category) {
            this.category = category;
        }

        @JsonGetter(value = "金额")
        public double getAmount() {
            return amount;
        }

        @JsonSetter(value = "金额")
        public void setAmount(double amount) {
            this.amount = amount;
        }

        @JsonGetter(value = "核算数量")
        public int getQuantity() {
            return quantity;
        }

        @JsonSetter(value = "核算数量")
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        @JsonGetter(value = "序列数值")
        public long getSerialNumber() {
            return serialNumber;
        }

        @JsonSetter(value = "序列数值")
        public void setSerialNumber(long serialNumber) {
            this.serialNumber = serialNumber;
        }

        @JsonGetter(value = "校验")
        public double getChecksum() {
            return checksum;
        }

        @JsonSetter(value = "校验")
        public void setChecksum(double checksum) {
            this.checksum = checksum;
        }

        @JsonGetter(value = "确认日期")
        public String getConfirmDate() {
            return confirmDate;
        }

        @JsonSetter(value = "确认日期")
        public void setConfirmDate(String confirmDate) {
            this.confirmDate = confirmDate;
        }

        @JsonGetter(value = "ddl")
        public String getDdl() {
            return ddl;
        }

        @JsonSetter(value = "ddl")
        public void setDdl(String ddl) {
            this.ddl = ddl;
        }

        @JsonGetter(value = "迷之数字")
        public BigDecimal getNazo() {
            return nazo;
        }

        @JsonSetter(value = "迷之数字")
        public void setNazo(BigDecimal nazo) {
            this.nazo = nazo;
        }

        @Override
        public String toString() {
            return "DatumRow{" +
                    "name='" + name + '\'' +
                    ", category='" + category + '\'' +
                    ", amount=" + amount +
                    ", quantity=" + quantity +
                    ", serialNumber=" + serialNumber +
                    ", checksum=" + checksum +
                    ", confirmDate=" + confirmDate +
                    ", ddl=" + ddl +
                    ", nazo=" + nazo +
                    '}';
        }
    }
}
