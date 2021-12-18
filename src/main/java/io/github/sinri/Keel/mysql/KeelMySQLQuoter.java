package io.github.sinri.Keel.mysql;

import java.util.List;

public class KeelMySQLQuoter {
    private final String quoted;

    public KeelMySQLQuoter(String x, Boolean withWildcards) {
        if (x == null) {
            quoted = "NULL";
        } else {
            if (withWildcards) {
                quoted = quoteEscapedString(escapeStringWithWildcards(x));
            } else {
                quoted = quoteEscapedString(escapeString(x));
            }
        }
    }

    public KeelMySQLQuoter(Number number) {
        if (number == null) {
            quoted = "NULL";
        } else {
            quoted = number.toString();
        }
    }

    public KeelMySQLQuoter(Boolean b) {
        if (b)
            quoted = "TRUE";
        else
            quoted = "FALSE";
    }

//    public KeelMySQLQuoter(String x) {
//        if (x == null) {
//            quoted = "NULL";
//        } else {
//            quoted = quoteEscapedString(escapeString(x));
//        }
//    }

    public KeelMySQLQuoter(Object o) {
        if (o == null) {
            quoted = "NULL";
        } else if (o instanceof Number) {
            quoted = o.toString();
        } else {
            // if (y instanceof String) or else
            quoted = quoteEscapedString(escapeString(String.valueOf(o.toString())));
        }
    }

    public KeelMySQLQuoter(List<?> list) {
        StringBuilder q = new StringBuilder();
        for (Object y : list) {
            if (q.length() > 0) {
                q.append(",");
            }
            if (y instanceof Number) {
                q.append(new KeelMySQLQuoter((Number) y));
            } else if (y instanceof String) {
                q.append(new KeelMySQLQuoter(y));
            } else {
                q.append(new KeelMySQLQuoter(y.toString()));
            }
        }
        quoted = "(" + q + ")";
    }

    public static String escapeString(String s) {
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

    public static String escapeStringWithWildcards(String s) {
        return escapeString(s)
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public static String quoteEscapedString(String s) {
        return "'" + s + "'";
    }

    @Override
    public String toString() {
        return quoted;
    }
}
