package io.github.sinri.keel.poi.excel;

import com.github.pjfanning.xlsx.StreamingReader;
import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;

/**
 * @since 3.2.11
 */
public class FileAccessOptions {
    private boolean withFormulaEvaluator;
    private File file;
    private StreamingReader.Builder streamingReaderBuilder;
    private InputStream inputStream;

    public FileAccessOptions() {
        withFormulaEvaluator = false;
        streamingReaderBuilder = null;
    }

    /**
     * <p>
     * excel-streaming-reader uses some Apache POI code under the hood.
     * That code uses memory and(or) temp files to store temporary data while it processes the xlsx.
     * With very large files, you will probably want to favour using temp files.
     * </p>
     * <p>
     * With StreamingReader.builder(), do not set setAvoidTempFiles(true).
     * You should also consider, tuning POI settings too.
     * </p>
     *
     * @since 3.2.11
     */
    public static void declareReadingVeryLargeExcelFiles() {
        org.apache.poi.openxml4j.util.ZipInputStreamZipEntrySource.setThresholdBytesForTempFiles(16384); //16KB
        org.apache.poi.openxml4j.opc.ZipPackage.setUseTempFilePackageParts(true);
    }

    boolean isWithFormulaEvaluator() {
        return this.withFormulaEvaluator;
    }

    public FileAccessOptions setWithFormulaEvaluator(boolean withFormulaEvaluator) {
        this.withFormulaEvaluator = withFormulaEvaluator;
        return this;
    }

    public File getFile() {
        return this.file;
    }

    public FileAccessOptions setFile(@NotNull File file) {
        this.file = file;
        return this;
    }

    public FileAccessOptions setFile(@NotNull String filePath) {
        this.file = new File(filePath);
        return this;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public FileAccessOptions setInputStream(@NotNull InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public boolean isUseStreamReading() {
        return this.streamingReaderBuilder != null;
    }

    public StreamingReader.Builder getStreamingReaderBuilder() {
        return streamingReaderBuilder;
    }

    /**
     * This is designed to read huge XLSX files with `pjfanning::excel-streaming-reader`.
     * <p>
     * You may access cells randomly within a row, as the entire row is cached.
     * However, there is no way to randomly access rows.
     * As this is a streaming implementation, only a small number of rows are kept in memory at any given time.
     * </p>
     * <p>
     * Consider to handle with Temp File Shared Strings and Temp File Comments.
     * </p>
     *
     * @see <a href="https://github.com/pjfanning/excel-streaming-reader">PJFANNING::ExcelStreamingReader</a>
     */
    public FileAccessOptions setStreamingReaderBuilder(@NotNull Handler<StreamingReader.Builder> streamingReaderBuilderHandler) {
        this.streamingReaderBuilder = new StreamingReader.Builder();

        // number of rows to keep in memory (defaults to 10)
        streamingReaderBuilder.rowCacheSize(32);
        // buffer size (in bytes) to use when reading InputStream to file (defaults to 1024)
        streamingReaderBuilder.bufferSize(10240);

        streamingReaderBuilderHandler.handle(streamingReaderBuilder);
        return this;
    }
}
