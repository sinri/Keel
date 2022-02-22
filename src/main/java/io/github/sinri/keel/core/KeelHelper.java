package io.github.sinri.keel.core;

import io.vertx.core.buffer.Buffer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Keel Helper
 */
public class KeelHelper {
    /**
     * @param x         array
     * @param separator separator
     * @param <T>       the class of item in array
     * @return the joined string
     * @since 1.11
     */
    public static <T> String joinStringArray(T[] x, String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < x.length; i++) {
            if (i > 0) result.append(separator);
            result.append(x[i].toString());
        }
        return result.toString();
    }

    public static String joinStringArray(List<?> x, String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < x.size(); i++) {
            if (i > 0) result.append(separator);
            result.append(x.get(i).toString());
        }
        return result.toString();
    }

    /**
     * @param raw raw string
     * @return md5 with lower digits
     * @since 1.1
     */
    public static String md5(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(raw.getBytes());
            return encodeHexWithLowerDigits(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param raw raw string
     * @return MD5 with upper digits
     * @since 1.1
     */
    public static String MD5(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(raw.getBytes());
            return encodeHexWithUpperDigits(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    final static char[] HEX_DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    final static char[] HEX_DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * @param buffer
     * @param rowSize
     * @return
     * @since 1.11
     */
    public static String bufferToHexMatrix(Buffer buffer, int rowSize) {
        StringBuilder matrix = new StringBuilder();
        String s = encodeHexWithUpperDigits(buffer);
        for (int i = 0; i < s.length(); i += 2) {
            matrix.append(s, i, i + 2).append(" ");
            if ((i / 2) % rowSize == rowSize - 1) {
                matrix.append("\n");
            }
        }
        return matrix.toString();
    }

    private static String encodeHexWithDigits(final char[] HEX_DIGITS, Buffer buffer, int since, int length) {
        StringBuilder hex = new StringBuilder();
        for (int i = since; i < since + length; i++) {
            hex
                    .append(HEX_DIGITS[(0xF0 & buffer.getByte(i)) >>> 4])
                    .append(HEX_DIGITS[0x0F & buffer.getByte(i)])
            ;
        }
        return hex.toString();
    }

    /**
     * @param data
     * @return
     * @since 1.11
     */
    public static String encodeHexWithLowerDigits(final byte[] data) {
        return encodeHexWithLowerDigits(Buffer.buffer(data));
    }

    /**
     * @param buffer
     * @return
     * @since 1.11
     */
    public static String encodeHexWithLowerDigits(Buffer buffer) {
        return encodeHexWithLowerDigits(buffer, 0, buffer.length());
    }

    /**
     * @param buffer
     * @param since
     * @param length
     * @return
     * @since 1.11
     */
    public static String encodeHexWithLowerDigits(Buffer buffer, int since, int length) {
        return encodeHexWithDigits(HEX_DIGITS_LOWER, buffer, since, length);
    }

    /**
     * @param data
     * @return
     * @since 1.11
     */
    public static String encodeHexWithUpperDigits(final byte[] data) {
        return encodeHexWithUpperDigits(Buffer.buffer(data));
    }

    /**
     * @param buffer
     * @return
     * @since 1.11
     */
    public static String encodeHexWithUpperDigits(Buffer buffer) {
        return encodeHexWithUpperDigits(buffer, 0, buffer.length());
    }

    /**
     * @param buffer
     * @param since
     * @param length
     * @return
     * @since 1.11
     */
    public static String encodeHexWithUpperDigits(Buffer buffer, int since, int length) {
        return encodeHexWithDigits(HEX_DIGITS_UPPER, buffer, since, length);
    }
}
