package io.github.sinri.keel.poi.csv;

import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;

/**
 * @since 3.1.1 Technical Preview
 */
@TechnicalPreview(since = "3.1.1")
public class KeelCsvReader {
    //private Charset charset = StandardCharsets.UTF_8;
    private final BufferedReader br;
    //private final @Nonnull InputStream inputStream;
    private String separator = ",";

    public KeelCsvReader(@Nonnull InputStream inputStream, Charset charset) {
        this(new BufferedReader(new InputStreamReader(inputStream, charset)));
    }

    public KeelCsvReader(@Nonnull BufferedReader br) {
        this.br = br;
    }

    public static Future<KeelCsvReader> create(@Nonnull InputStream inputStream, @Nonnull Charset charset) {
        var x = new KeelCsvReader(inputStream, charset);
        return Future.succeededFuture(x);
    }

    public static Future<KeelCsvReader> create(@Nonnull File file, @Nonnull Charset charset) {
        return Future.succeededFuture()
                .compose(v -> {
                    try {
                        var fis = new FileInputStream(file);
                        return create(fis, charset);
                    } catch (IOException e) {
                        return Future.failedFuture(e);
                    }
                });

    }

    public static Future<KeelCsvReader> create(@Nonnull String file, @Nonnull Charset charset) {
        return create(new File(file), charset);
    }

    public KeelCsvReader setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public @Nullable CsvRow blockReadRow() throws IOException {
        CsvRow row = null;

        /*
         * Three options: 0,1,2
         * > aaa,bbb
         * < 0000000
         * > ,"aa""bb",
         * < 0111211122
         */
        int quoterFlag = 0;
        String buffer = null;

        String line;
        while (true) {
            line = br.readLine();

            if (line == null) {
                // no more chars...
                if (row == null) return null;
                else {
                    if (buffer != null) {
                        row.addCell(new CsvCell(buffer));
                    }
                    break;
                }
            }

            if (row == null) row = new CsvRow();
            if (buffer == null) buffer = "";
            else {
                buffer += "\n";
            }

            for (int i = 0; i < line.length(); i++) {
                var singleString = line.substring(i, i + 1);
                if (singleString.equals("\"")) {
                    if (quoterFlag == 0) {
                        quoterFlag = 1;
                    } else if (quoterFlag == 1) {
                        quoterFlag = 2;
                    } else {
                        buffer += singleString;
                        quoterFlag = 1;
                    }
                } else if (singleString.equals(separator)) {
                    if (quoterFlag == 0 || quoterFlag == 2) {
                        // buffer to cell
                        row.addCell(new CsvCell(buffer));
                        quoterFlag = 0;
                        buffer = "";
                    } else {
                        buffer += singleString;
                    }
                } else {
                    buffer += singleString;
                }
            }

            // now this line ends
            if (quoterFlag == 0 || quoterFlag == 2) {
                // the row ends within this line
                row.addCell(new CsvCell(buffer));
                break;
            } else {
                // the row expends to the next line
            }
        }

        return row;
    }

    public Future<CsvRow> readRow() {
        return Future.succeededFuture()
                .compose(v -> {
                    try {
                        var row = this.blockReadRow();
                        return Future.succeededFuture(row);
                    } catch (IOException e) {
                        return Future.failedFuture(e);
                    }
                });
    }

    public void blockClose() throws IOException {
        this.br.close();
    }

    public Future<Void> close() {
        return Future.succeededFuture()
                .compose(v -> {
                    try {
                        blockClose();
                        return Future.succeededFuture();
                    } catch (IOException e) {
                        return Future.failedFuture(e);
                    }
                });
    }

}
