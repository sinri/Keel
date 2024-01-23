package io.github.sinri.keel.poi.excel;

import io.github.sinri.keel.core.ValueBox;
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
    protected @Nonnull Workbook autoWorkbook;

    /**
     * @param workbook The generated POI Workbook Implementation.
     * @since 3.0.20
     */
    public KeelSheets(@Nonnull Workbook workbook) {
        this(workbook, false);
    }

    /**
     * Create a new Sheets.
     */
    public KeelSheets() {
        this(null, false);
    }

    /**
     * Open an existed workbook or create.
     * Not use stream-write mode by default.
     *
     * @param workbook if null, create a new Sheets; otherwise, use it.
     * @since 3.1.3
     */
    public KeelSheets(@Nullable Workbook workbook, boolean withFormulaEvaluator) {
        autoWorkbook = Objects.requireNonNullElseGet(workbook, XSSFWorkbook::new);
        if (withFormulaEvaluator) {
            formulaEvaluator = autoWorkbook.getCreationHelper().createFormulaEvaluator();
        } else {
            formulaEvaluator = null;
        }
    }

    /**
     * @since 3.0.20
     */
    public static KeelSheets factory(@Nonnull String file) {
        return factory(file, false);
    }

    /**
     * @param file
     * @param withFormulaEvaluator
     * @return
     * @since 3.1.4
     */
    public static KeelSheets factory(@Nonnull String file, boolean withFormulaEvaluator) {
        return factory(new File(file), withFormulaEvaluator);
    }

    /**
     * @since 3.0.20
     */
    public static KeelSheets factory(@Nonnull File file) {
        return factory(file, false);
    }

    /**
     * @param file
     * @param withFormulaEvaluator
     * @return
     * @since 3.1.4
     */
    public static KeelSheets factory(@Nonnull File file, boolean withFormulaEvaluator) {
        try {
            return new KeelSheets(WorkbookFactory.create(file), withFormulaEvaluator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 3.0.20
     */
    public static KeelSheets factory(@Nonnull InputStream inputStream) {
        return factory(inputStream, false);
    }

    /**
     * @param inputStream
     * @param withFormulaEvaluator
     * @return
     * @since 3.1.4
     */
    public static KeelSheets factory(@Nonnull InputStream inputStream, boolean withFormulaEvaluator) {
        try {
            return new KeelSheets(WorkbookFactory.create(inputStream), withFormulaEvaluator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 3.0.20 The great DAN and HONG discovered an issue with POI Factory Mode.
     */
    public static KeelSheets autoGenerate(@Nonnull InputStream inputStream) {
        return autoGenerate(inputStream, false);
    }

    /**
     * @param inputStream
     * @param withFormulaEvaluator
     * @return
     * @since 3.1.4
     */
    public static KeelSheets autoGenerate(@Nonnull InputStream inputStream, boolean withFormulaEvaluator) {
        Workbook workbook;
        try {
            // XLSX
            workbook = new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            try {
                // XLS
                workbook = new HSSFWorkbook(inputStream);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return new KeelSheets(workbook, withFormulaEvaluator);
    }

    /**
     * @since 3.1.1
     */
    public static KeelSheets autoGenerateXLSX() {
        return new KeelSheets(new XSSFWorkbook());
    }

    /**
     * @param withFormulaEvaluator
     * @return
     * @since 3.1.4
     */
    public static KeelSheets autoGenerateXLSX(boolean withFormulaEvaluator) {
        return new KeelSheets(new XSSFWorkbook(), withFormulaEvaluator);
    }

    /**
     * @since 3.1.1
     */
    public static KeelSheets autoGenerateXLS() {
        return new KeelSheets(new HSSFWorkbook());
    }

    /**
     * @param withFormulaEvaluator
     * @return
     * @since 3.1.4
     */
    public static KeelSheets autoGenerateXLS(boolean withFormulaEvaluator) {
        return new KeelSheets(new HSSFWorkbook(), withFormulaEvaluator);
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
        return this.generateReaderForSheet(sheetName, true);
    }

    /**
     * @since 3.1.4
     */
    public KeelSheet generateReaderForSheet(@Nonnull String sheetName, boolean parseFormulaCellToValue) {
        var sheet = this.getWorkbook().getSheet(sheetName);
        ValueBox<FormulaEvaluator> formulaEvaluatorValueBox = new ValueBox<>();
        if (parseFormulaCellToValue) {
            formulaEvaluatorValueBox.setValue(this.formulaEvaluator);
        }
        return new KeelSheet(sheet, formulaEvaluatorValueBox);
    }

    public KeelSheet generateReaderForSheet(int sheetIndex) {
        return this.generateReaderForSheet(sheetIndex, true);
    }

    /**
     * @since 3.1.4
     */
    public KeelSheet generateReaderForSheet(int sheetIndex, boolean parseFormulaCellToValue) {
        var sheet = this.getWorkbook().getSheetAt(sheetIndex);
        ValueBox<FormulaEvaluator> formulaEvaluatorValueBox = new ValueBox<>();
        if (parseFormulaCellToValue) {
            formulaEvaluatorValueBox.setValue(this.formulaEvaluator);
        }
        return new KeelSheet(sheet, formulaEvaluatorValueBox);
    }

    public KeelSheet generateWriterForSheet(@Nonnull String sheetName, Integer pos) {
        Sheet sheet = this.getWorkbook().createSheet(sheetName);
        if (pos != null) {
            this.getWorkbook().setSheetOrder(sheetName, pos);
        }
        return new KeelSheet(sheet, new ValueBox<>(this.formulaEvaluator));
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
