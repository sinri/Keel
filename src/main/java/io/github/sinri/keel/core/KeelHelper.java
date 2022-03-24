package io.github.sinri.keel.core;

import io.github.sinri.keel.core.properties.KeelOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Keel Helper
 * It provides some static methods to support the widely used functions.
 */
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
     */
    public static String joinStringArray(List<?> x, String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < x.size(); i++) {
            if (i > 0) result.append(separator);
            result.append(x.get(i).toString());
        }
        return result.toString();
    }

    /**
     * 获取raw对应的以数字和小写字母描述的MD5摘要值。
     *
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
     * 获取raw对应的以数字和大写字母描述的MD5摘要值。
     *
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
     * 获取一个Buffer的十六进制表达，每个字节以两个字符表示（字母大写）。
     * 字节间空格分隔；每行容纳一定量的字节数。
     *
     * @param buffer  an instance of Buffer defined in Vertx
     * @param rowSize how many bytes in one row
     * @return the matrix of hex as string
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
     * @param data an array of byte
     * @return expression with hex using lower digits as string
     * @since 1.11
     */
    public static String encodeHexWithLowerDigits(final byte[] data) {
        return encodeHexWithLowerDigits(Buffer.buffer(data));
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @return expression with hex using lower digits as string
     * @since 1.11
     */
    public static String encodeHexWithLowerDigits(Buffer buffer) {
        return encodeHexWithLowerDigits(buffer, 0, buffer.length());
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @param since  the start index
     * @param length the length
     * @return expression of the substring with hex using lower digits as string
     * @since 1.11
     */
    public static String encodeHexWithLowerDigits(Buffer buffer, int since, int length) {
        return encodeHexWithDigits(HEX_DIGITS_LOWER, buffer, since, length);
    }

    /**
     * @param data an array of bytes
     * @return expression with hex using upper digits as string
     * @since 1.11
     */
    public static String encodeHexWithUpperDigits(final byte[] data) {
        return encodeHexWithUpperDigits(Buffer.buffer(data));
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @return expression of the substring with hex using upper digits as string
     * @since 1.11
     */
    public static String encodeHexWithUpperDigits(Buffer buffer) {
        return encodeHexWithUpperDigits(buffer, 0, buffer.length());
    }

    /**
     * @param buffer an instance of Buffer defined in Vertx
     * @param since  the start index
     * @param length the length
     * @return expression of the substring with hex using upper digits as string
     * @since 1.11
     */
    public static String encodeHexWithUpperDigits(Buffer buffer, int since, int length) {
        return encodeHexWithDigits(HEX_DIGITS_UPPER, buffer, since, length);
    }

    /**
     * @param jsonObject JsonObject
     * @param keychain   List of nested keys
     * @param value      value to write
     * @return target json object
     * @since 1.11
     */
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
        try {
            return Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            if (seekInsideJarWhenNotFound) {
                URL resource = KeelOptions.class.getClassLoader().getResource(filePath);
                if (resource == null) {
                    throw new IOException("Embedded one is not found after not found in FS: " + filePath, e);
                }
                String file = resource.getFile();
                return Files.readAllBytes(Path.of(file));
            } else {
                throw e;
            }
        }
    }

    /**
     * @param method
     * @param classOfAnnotation
     * @param defaultAnnotation
     * @param <T>               class of target annotation
     * @return target annotation
     * @since 1.13
     */
    public static <T extends Annotation> T getAnnotationOfMethod(Method method, Class<T> classOfAnnotation, T defaultAnnotation) {
        T annotation = method.getAnnotation(classOfAnnotation);
        if (annotation == null) {
            return defaultAnnotation;
        }
        return annotation;
    }

    /**
     * Seek in JAR!
     *
     * @param root ends with '/'
     * @return list of JarEntry
     */
    public static List<JarEntry> traversalInJar(String root) {
        List<JarEntry> jarEntryList = new ArrayList<>();
        try {
            // should root ends with '/'?
            URL url = KeelHelper.class.getClassLoader().getResource(root);
            System.out.println(url);
            if (url == null) {
                throw new RuntimeException("Resource is not found");
            }
            if (!url.toString().contains("!/")) {
                throw new RuntimeException("Resource is not in JAR");
            }
            String jarPath = url.toString().substring(0, url.toString().indexOf("!/") + 2);
            System.out.println(jarPath);

            URL jarURL = new URL(jarPath);
            JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntries = jarFile.entries();

            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();
//                String name = entry.getName();
//                if (name.startsWith(root) && !entry.isDirectory()) {
//                    doWithInputStream(getClass().getClassLoader().getResourceAsStream(name));
//                }
                if (entry.getName().startsWith(root)) {
                    jarEntryList.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jarEntryList;
    }
}
