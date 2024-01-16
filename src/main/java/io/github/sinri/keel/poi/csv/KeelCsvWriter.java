package io.github.sinri.keel.poi.csv;

import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.1.1 Technical Preview
 */
@TechnicalPreview(since = "3.1.1")
public class KeelCsvWriter {
    private final OutputStream outputStream;
    private String separator = ",";
    private Charset charset = StandardCharsets.UTF_8;

    public KeelCsvWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public static Future<KeelCsvWriter> create(@Nonnull String file) {
        return create(new File(file));
    }

    public static Future<KeelCsvWriter> create(@Nonnull File file) {
        return Future.succeededFuture()
                .compose(v -> {
                    try {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        return create(outputStream);
                    } catch (FileNotFoundException e) {
                        return Future.failedFuture(e);
                    }
                });
    }

    public static Future<KeelCsvWriter> create(@Nonnull OutputStream outputStream) {
        var x = new KeelCsvWriter(outputStream);
        return Future.succeededFuture(x);
    }

    public KeelCsvWriter setSeparator(@Nonnull String separator) {
        this.separator = separator;
        return this;
    }

    public KeelCsvWriter setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public void blockWriteRow(@Nonnull List<String> list) throws IOException {
        List<String> components = new ArrayList<>();
        list.forEach(item -> {
            components.add(quote(item));
        });
        var line = KeelHelpers.stringHelper().joinStringArray(components, separator) + "\n";
        //System.out.println(line);
        outputStream.write(line.getBytes(charset));
    }

    public Future<Void> writeRow(List<String> row) {
        return Future.succeededFuture()
                .compose(v -> {
                    try {
                        blockWriteRow(row);
                        return Future.succeededFuture();
                    } catch (IOException e) {
                        return Future.failedFuture(e);
                    }
                });
    }

    public void blockClose() throws IOException {
        this.outputStream.close();
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

    private String quote(@Nullable String s) {
        if (s == null) {
            s = "";
        }
        if (s.contains("\"") || s.contains("\n") || s.contains(separator)) {
            return "\"" + s.replaceAll("\"", "\"\"") + "\"";
        } else {
            return s;
        }
    }
}
