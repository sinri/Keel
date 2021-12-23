package io.github.sinri.keel.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Keel Helper
 */
public class KeelHelper {
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

    private static String encodeHexWithLowerDigits(final byte[] data) {
        final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        final int dataOffset = 0;
        final int dataLen = data.length;
        final char[] out = new char[dataLen << 1];
        final int outOffset = 0;
        // two characters form the hex value.
        for (int i = dataOffset, j = outOffset; i < dataOffset + dataLen; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }

        return new String(out);
    }

    private static String encodeHexWithUpperDigits(final byte[] data) {
        final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        final int dataOffset = 0;
        final int dataLen = data.length;
        final char[] out = new char[dataLen << 1];
        final int outOffset = 0;
        // two characters form the hex value.
        for (int i = dataOffset, j = outOffset; i < dataOffset + dataLen; i++) {
            out[j++] = DIGITS_UPPER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_UPPER[0x0F & data[i]];
        }

        return new String(out);
    }
}
