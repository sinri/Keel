package io.github.sinri.keel.test.v1.mysql;

public class MySQLEscapeTest {
    public static void main(String[] args) {
        testEscape();
    }

    protected static void testEscape() {
        StringBuilder sb = new StringBuilder();
        sb.append('\0').append('\'').append('"').append('\b').append('\n').append('\t').append('\r')
                .append(Character.toChars(26)).append('\\').append('_').append('%');

        String raw = sb.toString();
        System.out.println("raw: " + raw);
        System.out.println("escapeStringForMySQL: " + escapeStringForMySQL(raw));
        System.out.println("escapeWildcardsForMySQL: " + escapeWildcardsForMySQL(raw));
    }

    private static String escapeStringForMySQL(String s) {
        return s.replace("\\", "\\\\")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace(new String(Character.toChars(26)), "\\Z")
                .replace(new String(Character.toChars(0)), "\\0")
                .replace("'", "\\'")
                .replace("\"", "\\\"");
    }

    private static String escapeWildcardsForMySQL(String s) {
        return escapeStringForMySQL(s)
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
