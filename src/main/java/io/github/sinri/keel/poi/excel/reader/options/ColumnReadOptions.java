package io.github.sinri.keel.poi.excel.reader.options;


import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;

public class ColumnReadOptions {
    private boolean formatDateTime = true;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ColumnType columnType = ColumnType.String;
    @Nullable
    private String columnName;

    public static ColumnReadOptions build(String columnName, ColumnType columnType) {
        return new ColumnReadOptions().setColumnType(columnType).setColumnName(columnName);
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public ColumnReadOptions setColumnType(ColumnType columnType) {
        this.columnType = columnType;
        return this;
    }

    @Nullable
    public String getColumnName() {
        return columnName;
    }

    public ColumnReadOptions setColumnName(@Nullable String columnName) {
        this.columnName = columnName;
        return this;
    }

    public boolean isFormatDateTime() {
        return formatDateTime;
    }

    public ColumnReadOptions setFormatDateTime(boolean formatDateTime) {
        this.formatDateTime = formatDateTime;
        return this;
    }

    public SimpleDateFormat getDateTimeFormat() {
        return dateTimeFormat;
    }

    public ColumnReadOptions setDateTimeFormat(SimpleDateFormat dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
        return this;
    }
}
