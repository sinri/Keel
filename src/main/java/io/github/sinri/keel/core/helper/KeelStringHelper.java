package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.Keel;
import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @since 2.6
 */
public class KeelStringHelper {
    private static final KeelStringHelper instance = new KeelStringHelper();

    private KeelStringHelper() {

    }

    public static KeelStringHelper getInstance() {
        return instance;
    }

    /**
     * 给定一个数组x，用separator作为分隔符，将x中的所有元素的字符串化值拼接起来。
     *
     * @param x         an array
     * @param separator separator
     * @param <T>       the class of item in array
     * @return the joined string
     * @since 1.11
     */
    public <T> String joinStringArray(T[] x, String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < x.length; i++) {
            if (i > 0) result.append(separator);
            result.append(x[i].toString());
        }
        return result.toString();
    }

    /**
     * 给定一个列表x，用separator作为分隔符，将x中的所有元素的字符串化值拼接起来。
     *
     * @param x         a list
     * @param separator separator
     * @return the joined string
     * @since 2.0 list -> collection
     */
    public String joinStringArray(Collection<?> x, String separator) {
        StringBuilder result = new StringBuilder();

        final int[] i = {0};
        x.forEach(item -> {
            if (i[0] > 0) result.append(separator);
            result.append(item.toString());
            i[0] += 1;
        });

        return result.toString();
    }

    /**
     * 获取raw对应的以数字和小写字母描述的MD5摘要值。
     *
     * @param raw raw string
     * @return md5 with lower digits
     * @since 1.1
     * @since 2.8 use digestHelper
     */
    @Deprecated(since = "2.8")
    public String md5(String raw) {
        return Keel.digestHelper().md5(raw);
    }

    /**
     * 获取raw对应的以数字和大写字母描述的MD5摘要值。
     *
     * @param raw raw string
     * @return MD5 with upper digits
     * @since 1.1
     * @since 2.8 use digestHelper
     */
    @Deprecated(since = "2.8")
    public String MD5(String raw) {
        return Keel.digestHelper().MD5(raw);
    }

    /**
     * 获取一个Buffer的十六进制表达，每个字节以两个字符表示（字母大写）。
     * 字节间空格分隔；每行容纳一定量的字节数。
     *
     * @param buffer  an instance of Buffer defined in Vertx
     * @param rowSize how many bytes in one row
     * @return the matrix of hex as string
     * @since 1.11
     */
    public String bufferToHexMatrix(Buffer buffer, int rowSize) {
        StringBuilder matrix = new StringBuilder();
        String s = Keel.binaryHelper().encodeHexWithUpperDigits(buffer);
        for (int i = 0; i < s.length(); i += 2) {
            matrix.append(s, i, i + 2).append(" ");
            if ((i / 2) % rowSize == rowSize - 1) {
                matrix.append("\n");
            }
        }
        return matrix.toString();
    }


    /**
     * @since 2.7
     */
    public String fromUnderScoreCaseToCamelCase(String underScoreCase) {
        if (underScoreCase == null) {
            return null;
        }
        String[] parts = underScoreCase.toLowerCase().split("[\\s_]");
        List<String> camel = new ArrayList<>();
        for (var part : parts) {
            if (part != null && !part.isEmpty() && !part.isBlank()) {
                camel.add(part.substring(0, 1).toUpperCase() + part.substring(1));
            }
        }
        return Keel.stringHelper().joinStringArray(camel, "");
    }

    /**
     * @since 2.7
     */
    public String fromCamelCaseToUserScoreCase(String camelCase) {
        if (camelCase == null) {
            return null;
        }
        if (camelCase.isEmpty() || camelCase.isBlank()) {
            return "";
        }
        if (camelCase.length() == 1) {
            return camelCase.toLowerCase();
        }
        List<String> parts = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            String current = camelCase.substring(i, i + 1);
            if (current.matches("[\\s_]")) continue;
            if (part.length() == 0) {
                part.append(current.toLowerCase());
            } else {
                if (current.matches("[A-Z]")) {
                    parts.add(part.toString());
                    part = new StringBuilder();
                }
                part.append(current.toLowerCase());
            }
        }
        if (part.length() > 0) {
            parts.add(part.toString());
        }
        return Keel.stringHelper().joinStringArray(parts, "_");
    }


}
