package io.github.sinri.keel.helper;

import io.github.sinri.keel.helper.encryption.base32.Base32;
import io.vertx.core.buffer.Buffer;

import javax.annotation.Nonnull;
import java.util.Base64;

public class KeelBinaryHelper {
    final static char[] HEX_DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    final static char[] HEX_DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final KeelBinaryHelper instance = new KeelBinaryHelper();

    private KeelBinaryHelper() {

    }

    static KeelBinaryHelper getInstance() {
        return instance;
    }

    private @Nonnull String encodeHexWithDigits(final char[] HEX_DIGITS, @Nonnull Buffer buffer, int since, int length) {
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
     * @param data an array of byte
     * @return expression with hex using lower digits as string
     * @since 1.11
     */
    public String encodeHexWithLowerDigits(@Nonnull final byte[] data) {
        return encodeHexWithLowerDigits(Buffer.buffer(data));
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @return expression with hex using lower digits as string
     * @since 1.11
     */
    public @Nonnull String encodeHexWithLowerDigits(@Nonnull Buffer buffer) {
        return encodeHexWithLowerDigits(buffer, 0, buffer.length());
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @param since  the start index
     * @param length the length
     * @return expression of the substring with hex using lower digits as string
     * @since 1.11
     */
    public @Nonnull String encodeHexWithLowerDigits(@Nonnull Buffer buffer, int since, int length) {
        return encodeHexWithDigits(HEX_DIGITS_LOWER, buffer, since, length);
    }

    /**
     * @param data an array of bytes
     * @return expression with hex using upper digits as string
     * @since 1.11
     */
    public @Nonnull String encodeHexWithUpperDigits(@Nonnull final byte[] data) {
        return encodeHexWithUpperDigits(Buffer.buffer(data));
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @return expression of the substring with hex using upper digits as string
     * @since 1.11
     */
    public @Nonnull String encodeHexWithUpperDigits(@Nonnull Buffer buffer) {
        return encodeHexWithUpperDigits(buffer, 0, buffer.length());
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @param since  the start index
     * @param length the length
     * @return expression of the substring with hex using upper digits as string
     * @since 1.11
     */
    public @Nonnull String encodeHexWithUpperDigits(@Nonnull Buffer buffer, int since, int length) {
        return encodeHexWithDigits(HEX_DIGITS_UPPER, buffer, since, length);
    }

    /**
     * @since 2.9.4
     */
    public @Nonnull byte[] decodeWithBase64(@Nonnull byte[] bytes) {
        return Base64.getDecoder().decode(bytes);
    }

    /**
     * @since 2.9.4
     */
    @Nonnull
    public byte[] encodeWithBase64(@Nonnull byte[] bytes) {
        return Base64.getEncoder().encode(bytes);
    }

    /**
     * @since 2.9.4
     */
    @Nonnull
    public String encodeWithBase64ToString(@Nonnull byte[] bytes) {
        return new String(encodeWithBase64(bytes));
    }

    /**
     * @since 2.9.4
     */
    public @Nonnull byte[] encodeWithBase32(@Nonnull byte[] bytes) {
        return encodeWithBase32ToString(bytes).getBytes();
    }

    /**
     * @since 2.9.4
     */
    @Nonnull
    public String encodeWithBase32ToString(@Nonnull byte[] bytes) {
        return Base32.encode(bytes);
    }

    /**
     * @since 2.9.4
     */
    public @Nonnull byte[] decodeWithBase32(@Nonnull byte[] bytes) {
        return Base32.decode(new String(bytes));
    }

    /**
     * @since 2.9.4
     */
    public @Nonnull String decodeWithBase32ToString(@Nonnull byte[] bytes) {
        return new String(decodeWithBase32(bytes));
    }
}
