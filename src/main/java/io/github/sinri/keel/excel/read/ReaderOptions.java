package io.github.sinri.keel.excel.read;

import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * @since 3.0.8
 */
public class ReaderOptions {
    @Nullable
    private String fileName;
    @Nullable
    private InputStream fileInputStream;
    @Nullable
    private Integer sheetNo;
    @Nullable
    private String sheetName;
    private int headRowNumber = 1;

    public InputStream getFileInputStream() {
        return fileInputStream;
    }

    public ReaderOptions setFileInputStream(InputStream fileInputStream) {
        this.fileInputStream = fileInputStream;
        return this;
    }

    public int getHeadRowNumber() {
        return headRowNumber;
    }

    public ReaderOptions setHeadRowNumber(int headRowNumber) {
        this.headRowNumber = headRowNumber;
        return this;
    }

    public Integer getSheetNo() {
        return sheetNo;
    }

    public ReaderOptions setSheetNo(int sheetNo) {
        this.sheetNo = sheetNo;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ReaderOptions setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getSheetName() {
        return sheetName;
    }

    public ReaderOptions setSheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }
}
