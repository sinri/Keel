package io.github.sinri.keel.poi.excel;

import io.github.sinri.keel.core.TechnicalPreview;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.annotation.Nonnull;
import java.io.*;

@TechnicalPreview(since = "3.0.13")
public class KeelSheets implements AutoCloseable {
    protected @Nonnull Workbook autoWorkbook;

    public KeelSheets(@Nonnull String file) {
        this(new File(file));
    }

    public KeelSheets(@Nonnull File file) {
        try {
            autoWorkbook = WorkbookFactory.create(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public KeelSheets(@Nonnull InputStream inputStream) {
        try {
            autoWorkbook = WorkbookFactory.create(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new Sheets.
     * Not use stream-write mode by default.
     */
    public KeelSheets() {
        autoWorkbook = new XSSFWorkbook();
    }

    public KeelSheets useStreamWrite() {
        if (autoWorkbook instanceof XSSFWorkbook) {
            autoWorkbook = new SXSSFWorkbook((XSSFWorkbook) autoWorkbook);
        } else {
            throw new IllegalStateException("Now autoWorkbook is not an instance of XSSFWorkbook.");
        }
        return this;
    }

    public KeelSheet generateReaderForSheet(@Nonnull String sheetName) {
        var sheet = this.getWorkbook().getSheet(sheetName);
        return new KeelSheet(sheet);
    }

    public KeelSheet generateReaderForSheet(int sheetIndex) {
        var sheet = this.getWorkbook().getSheetAt(sheetIndex);
        return new KeelSheet(sheet);
    }

    public KeelSheet generateWriterForSheet(@Nonnull String sheetName, Integer pos) {
        Sheet sheet = this.getWorkbook().createSheet(sheetName);
        if (pos != null) {
            this.getWorkbook().setSheetOrder(sheetName, pos);
        }
        return new KeelSheet(sheet);
    }

    public KeelSheet generateWriterForSheet(@Nonnull String sheetName) {
        return generateWriterForSheet(sheetName, null);
    }

    public int getSheetCount() {
        return autoWorkbook.getNumberOfSheets();
    }

    /**
     * @return Raw Apache POI Workbook instance.
     */
    @Nonnull
    public Workbook getWorkbook() {
        return autoWorkbook;
    }

    public void save(OutputStream outputStream) {
        try {
            autoWorkbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(File file) {
        try {
            save(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(String fileName) {
        save(new File(fileName));
    }

    @Override
    public void close() {
        try {
            autoWorkbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
