package io.github.sinri.keel.helper;

import io.github.sinri.keel.helper.encryption.base32.Base32;
import io.vertx.core.buffer.Buffer;

import java.util.*;

/**
 * @since 2.6
 */
public class KeelStringHelper {
    private static final KeelStringHelper instance = new KeelStringHelper();

    private KeelStringHelper() {

    }

    static KeelStringHelper getInstance() {
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
     * @since 2.0 list → collection
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
        String s = KeelHelpers.getInstance().binaryHelper().encodeHexWithUpperDigits(buffer);
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
        return KeelHelpers.getInstance().stringHelper().joinStringArray(camel, "");
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
        return KeelHelpers.getInstance().stringHelper().joinStringArray(parts, "_");
    }

    /**
     * @since 2.9
     */
    public String buildStackChainText(StackTraceElement[] stackTrace, Set<String> ignorableStackPackageSet) {
        StringBuilder sb = new StringBuilder();
        if (stackTrace != null) {
            String ignoringClassPackage = null;
            int ignoringCount = 0;
            for (StackTraceElement stackTranceItem : stackTrace) {
                String className = stackTranceItem.getClassName();
                String matchedClassPackage = null;
                for (var cp : ignorableStackPackageSet) {
                    if (className.startsWith(cp)) {
                        matchedClassPackage = cp;
                        break;
                    }
                }
                if (matchedClassPackage == null) {
                    if (ignoringCount > 0) {
                        sb.append("\t\t")
                                .append("[").append(ignoringCount).append("] ")
                                .append(ignoringClassPackage)
                                .append(System.lineSeparator());

                        ignoringClassPackage = null;
                        ignoringCount = 0;
                    }

                    sb.append("\t\t")
                            .append(stackTranceItem.getClassName())
                            .append(".")
                            .append(stackTranceItem.getMethodName())
                            .append(" (")
                            .append(stackTranceItem.getFileName())
                            .append(":")
                            .append(stackTranceItem.getLineNumber())
                            .append(")")
                            .append(System.lineSeparator());
                } else {
                    if (ignoringCount > 0) {
                        if (ignoringClassPackage.equals(matchedClassPackage)) {
                            ignoringCount += 1;
                        } else {
                            sb.append("\t\t")
                                    .append("[").append(ignoringCount).append("] ")
                                    .append(ignoringClassPackage)
                                    .append(System.lineSeparator());

                            ignoringClassPackage = matchedClassPackage;
                            ignoringCount = 1;
                        }
                    } else {
                        ignoringClassPackage = matchedClassPackage;
                        ignoringCount = 1;
                    }
                }
            }
            if (ignoringCount > 0) {
                sb.append("\t\t")
                        .append("[").append(ignoringCount).append("] ")
                        .append(ignoringClassPackage)
                        .append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    /**
     * @since 2.9
     */
    public String buildStackChainText(StackTraceElement[] stackTrace) {
        return buildStackChainText(stackTrace, Set.of());
    }


    /**
     * @since 2.9
     */
    public String renderThrowableChain(Throwable throwable, Set<String> ignorableStackPackageSet) {
        if (throwable == null) return "";
        Throwable cause = throwable.getCause();
        StringBuilder sb = new StringBuilder();
        sb
                .append("\t")
                .append(throwable.getClass().getName())
                .append(": ")
                .append(throwable.getMessage())
                .append(System.lineSeparator())
                .append(buildStackChainText(throwable.getStackTrace(), ignorableStackPackageSet));

        while (cause != null) {
            sb
                    .append("\t↑ ")
                    .append(cause.getClass().getName())
                    .append(": ")
                    .append(cause.getMessage())
                    .append(System.lineSeparator())
                    .append(buildStackChainText(cause.getStackTrace(), ignorableStackPackageSet))
            ;

            cause = cause.getCause();
        }

        return sb.toString();
    }

    /**
     * @since 2.9
     */
    public String renderThrowableChain(Throwable throwable) {
        return renderThrowableChain(throwable, Set.of());
    }

    /**
     * @since 2.9.4
     */
    public byte[] encodeWithBase64ToBytes(String s) {
        return KeelHelpers.getInstance().binaryHelper().encodeWithBase64(s.getBytes());
    }

    /**
     * @since 2.9.4
     */
    public String encodeWithBase64(String s) {
        return new String(encodeWithBase64ToBytes(s));
    }

    /**
     * @since 2.9.4
     */
    public byte[] decodeWithBase64ToBytes(String s) {
        return Base64.getDecoder().decode(s);
    }

    /**
     * @since 2.9.4
     */
    public String encodeWithBase32(String s) {
        return Base32.encode(s.getBytes());
    }

    /**
     * @since 2.9.4
     */
    public byte[] decodeWithBase32ToBytes(String s) {
        return Base32.decode(s);
    }

    /**
     * @since 2.9.4
     */
    public String decodeWithBase32(String s) {
        return new String(decodeWithBase32ToBytes(s));
    }
}
