package io.github.sinri.keel.core;

import io.github.sinri.keel.Keel;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * Keel Helper
 * It provides some static methods to support the widely used functions.
 */
@Deprecated(since = "2.6")
public class KeelHelper {
    /**
     * 给定一个数组x，用separator作为分隔符，将x中的所有元素的字符串化值拼接起来。
     *
     * @param x         an array
     * @param separator separator
     * @param <T>       the class of item in array
     * @return the joined string
     * @since 1.11
     */
    public static <T> String joinStringArray(T[] x, String separator) {
        return Keel.stringHelper().joinStringArray(x, separator);
    }

    /**
     * 给定一个列表x，用separator作为分隔符，将x中的所有元素的字符串化值拼接起来。
     *
     * @param x         a list
     * @param separator separator
     * @return the joined string
     * @since 2.0 list -> collection
     */
    public static String joinStringArray(Collection<?> x, String separator) {
        return Keel.stringHelper().joinStringArray(x, separator);
    }

    /**
     * 获取raw对应的以数字和小写字母描述的MD5摘要值。
     *
     * @param raw raw string
     * @return md5 with lower digits
     * @since 1.1
     */
    public static String md5(String raw) {
        return Keel.stringHelper().md5(raw);
    }

    /**
     * 获取raw对应的以数字和大写字母描述的MD5摘要值。
     *
     * @param raw raw string
     * @return MD5 with upper digits
     * @since 1.1
     */
    public static String MD5(String raw) {
        return Keel.stringHelper().MD5(raw);
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
    public static String bufferToHexMatrix(Buffer buffer, int rowSize) {
        return Keel.stringHelper().bufferToHexMatrix(buffer, rowSize);
    }

    /**
     * @param data an array of byte
     * @return expression with hex using lower digits as string
     * @since 1.11
     */
    public static String encodeHexWithLowerDigits(final byte[] data) {
        return Keel.stringHelper().encodeHexWithLowerDigits(Buffer.buffer(data));
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @return expression with hex using lower digits as string
     * @since 1.11
     */
    public static String encodeHexWithLowerDigits(Buffer buffer) {
        return Keel.stringHelper().encodeHexWithLowerDigits(buffer, 0, buffer.length());
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @param since  the start index
     * @param length the length
     * @return expression of the substring with hex using lower digits as string
     * @since 1.11
     */
    public static String encodeHexWithLowerDigits(Buffer buffer, int since, int length) {
        return Keel.stringHelper().encodeHexWithLowerDigits(buffer, since, length);
    }

    /**
     * @param data an array of bytes
     * @return expression with hex using upper digits as string
     * @since 1.11
     */
    public static String encodeHexWithUpperDigits(final byte[] data) {
        return Keel.stringHelper().encodeHexWithUpperDigits(Buffer.buffer(data));
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @return expression of the substring with hex using upper digits as string
     * @since 1.11
     */
    public static String encodeHexWithUpperDigits(Buffer buffer) {
        return Keel.stringHelper().encodeHexWithUpperDigits(buffer, 0, buffer.length());
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @param since  the start index
     * @param length the length
     * @return expression of the substring with hex using upper digits as string
     * @since 1.11
     */
    public static String encodeHexWithUpperDigits(Buffer buffer, int since, int length) {
        return Keel.stringHelper().encodeHexWithUpperDigits(buffer, since, length);
    }

    /**
     * @param jsonObject JsonObject
     * @param keychain   List of nested keys
     * @param value      value to write
     * @return target json object
     * @since 1.11
     */
    @Deprecated(since = "2.6")
    public static JsonObject writeIntoJsonObject(final JsonObject jsonObject, final List<String> keychain, final Object value) {
        if (keychain.size() <= 0) {
            return jsonObject;
        }
        if (keychain.size() == 1) {
            jsonObject.put(keychain.get(0), value);
            return jsonObject;
        }
        String x = keychain.get(0);
        if (jsonObject.getJsonObject(x) == null) {
            jsonObject.put(x, new JsonObject());
        }
        return writeIntoJsonObject(jsonObject.getJsonObject(x), keychain.subList(1, keychain.size()), value);
    }

    public static byte[] readFileAsByteArray(String filePath, boolean seekInsideJarWhenNotFound) throws IOException {
        return Keel.fileHelper().readFileAsByteArray(filePath, seekInsideJarWhenNotFound);
    }

    /**
     * @param filePath path string of the target file, or directory
     * @return the URL of target file; if not there, null return.
     */
    public static URL getUrlOfFileInJar(String filePath) {
        return Keel.fileHelper().getUrlOfFileInJar(filePath);
    }

    /**
     * @param <T> class of target annotation
     * @return target annotation
     * @since 1.13
     */
    public static <T extends Annotation> T getAnnotationOfMethod(Method method, Class<T> classOfAnnotation, T defaultAnnotation) {
        return Keel.reflectionHelper().getAnnotationOfMethod(method, classOfAnnotation, defaultAnnotation);
    }

    /**
     * Seek in JAR, under the root (exclusive)
     *
     * @param root ends with '/'
     * @return list of JarEntry
     */
    public static List<JarEntry> traversalInJar(String root) {
        return Keel.fileHelper().traversalInJar(root);
    }

    /**
     * @since 2.4
     */
    public static String getJsonForArrayWhoseItemsSorted(JsonArray array) {
        return Keel.jsonHelper().getJsonForArrayWhoseItemsSorted(array);
    }

    /**
     * @since 2.4
     */
    public static String getJsonForObjectWhoseItemKeysSorted(JsonObject object) {
        return Keel.jsonHelper().getJsonForObjectWhoseItemKeysSorted(object);
    }

    /**
     * @param format "yyyyMMdd" or "yyyy-MM-dd HH:mm:ss", etc. if null, return null
     * @return the date string or null
     * @since 2.6
     */
    public static String getCurrentDateExpression(String format) {
        return Keel.dateTimeHelper().getCurrentDateExpression(format);
    }

    /**
     * @since 2.6
     */
    public static String getDateExpression(Date date, String format) {
        return Keel.dateTimeHelper().getDateExpression(date, format);
    }
}
