package io.github.sinri.keel.poi.excel;

import io.github.sinri.keel.poi.excel.reader.KeelSheetReader;
import io.github.sinri.keel.poi.excel.reader.options.SheetReadOptions;
import io.github.sinri.keel.poi.excel.writer.KeelSheetWriter;
import io.vertx.core.Handler;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Objects;

/**
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 */
public class KeelSheets implements AutoCloseable {

    /**
     * @since 3.1.3
     */
    private final @Nullable FormulaEvaluator formulaEvaluator;
    protected final @Nonnull Workbook workbook;

    /**
     * Open an existed workbook or create, with formula evaluator if required.
     * Not use stream-write mode by default.
     *
     * @param workbook if null, create a new Sheets; otherwise, use it.
     * @since 3.1.3
     */
    private KeelSheets(@Nullable Workbook workbook, boolean withFormulaEvaluator) {
        this.workbook = Objects.requireNonNullElseGet(workbook, XSSFWorkbook::new);
        if (withFormulaEvaluator) {
            formulaEvaluator = this.workbook.getCreationHelper().createFormulaEvaluator();
        } else {
            formulaEvaluator = null;
        }
    }

    /**
     * @since 4.0.0
     */
    public static KeelSheets loadToRead(@Nonnull Handler<FileAccessOptions> fileAccessOptionsHandler) {
        FileAccessOptions fileAccessOptions = new FileAccessOptions();
        fileAccessOptionsHandler.handle(fileAccessOptions);
        return loadToRead(fileAccessOptions);
    }

    /**
     * @since 3.2.11
     */
    public static KeelSheets loadToRead(@Nonnull FileAccessOptions fileAccessOptions) {
        try {
            if (fileAccessOptions.isUseStreamReading()) {
                // use stream reading
                if (fileAccessOptions.getInputStream() != null) {
                    Workbook workbook = fileAccessOptions.getStreamingReaderBuilder().open(fileAccessOptions.getInputStream());
                    return new KeelSheets(workbook, false);
                } else if (fileAccessOptions.getFile() != null) {
                    Workbook workbook = fileAccessOptions.getStreamingReaderBuilder().open(fileAccessOptions.getFile());
                    return new KeelSheets(workbook, false);
                }
            } else {
                // use entirely reading
                if (fileAccessOptions.getInputStream() != null) {
                    Workbook workbook = WorkbookFactory.create(fileAccessOptions.getFile());
                    return new KeelSheets(workbook, fileAccessOptions.isWithFormulaEvaluator());
                } else if (fileAccessOptions.getFile() != null) {
                    Workbook workbook = WorkbookFactory.create(fileAccessOptions.getFile());
                    return new KeelSheets(workbook, fileAccessOptions.isWithFormulaEvaluator());
                }
            }
            throw new RuntimeException("No input source!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 4.0.0
     */
    public static KeelSheets createToWrite(@Nonnull FileWriteStyle fileWriteStyle) {
        return switch (fileWriteStyle) {
            case Xlsx -> new KeelSheets(new XSSFWorkbook(), false);
            case StreamingXlsx -> new KeelSheets(new SXSSFWorkbook(new XSSFWorkbook()), false);
            case Xls -> new KeelSheets(new HSSFWorkbook(), false);
        };
    }

    /**
     * @since 3.2.16
     */
    public KeelSheetReader generateReaderForSheet(@Nonnull String sheetName, @Nonnull Handler<SheetReadOptions> readOptionsHandler) {
        var sheet = this.getWorkbook().getSheet(sheetName);
        var readOptions = new SheetReadOptions();
        readOptions.setFormulaEvaluator(formulaEvaluator);
        readOptionsHandler.handle(readOptions);
        return new KeelSheetReader(sheet, readOptions);
    }

    /**
     * @since 3.2.16
     */
    public KeelSheetReader generateReaderForSheet(int sheetIndex, @Nonnull Handler<SheetReadOptions> readOptionsHandler) {
        var sheet = this.getWorkbook().getSheetAt(sheetIndex);
        var readOptions = new SheetReadOptions();
        readOptions.setFormulaEvaluator(formulaEvaluator);
        readOptionsHandler.handle(readOptions);
        return new KeelSheetReader(sheet, readOptions);
    }

    public KeelSheetWriter generateWriterForSheet(@Nonnull String sheetName, Integer pos) {
        Sheet sheet = this.getWorkbook().createSheet(sheetName);
        if (pos != null) {
            this.getWorkbook().setSheetOrder(sheetName, pos);
        }
        return new KeelSheetWriter(sheet);
    }

    public KeelSheetWriter generateWriterForSheet(@Nonnull String sheetName) {
        return generateWriterForSheet(sheetName, null);
    }

    public int getSheetCount() {
        return workbook.getNumberOfSheets();
    }

    /**
     * @return Raw Apache POI Workbook instance.
     */
    @Nonnull
    public Workbook getWorkbook() {
        return workbook;
    }

    public void save(OutputStream outputStream) {
        try {
            workbook.write(outputStream);
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
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
