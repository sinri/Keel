package io.github.sinri.keel.mysql;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class Quoter {
    private final @Nonnull String quoted;

    public Quoter(@Nullable String x, boolean withWildcards) {
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

    public Quoter(@Nullable Number number) {
        if (number == null) {
            quoted = "NULL";
        } else {
            quoted = number.toString();
        }
    }

    public Quoter(boolean b) {
        if (b)
            quoted = "TRUE";
        else
            quoted = "FALSE";
    }

    public Quoter(@Nullable String s) {
        if (s == null) {
            quoted = "NULL";
        } else {
            // if (y instanceof String) or else
            quoted = quoteEscapedString(escapeString(s));
        }
    }

    public Quoter(@Nonnull List<?> list) {
        StringBuilder q = new StringBuilder();
        for (Object y : list) {
            if (q.length() > 0) {
                q.append(",");
            }
            if (y instanceof Number) {
                q.append(new Quoter((Number) y));
            } else {
                q.append(new Quoter(y.toString()));
            }
        }
        quoted = "(" + q + ")";
    }

    public static @Nonnull String escapeString(@Nonnull String s) {
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

    public static @Nonnull String escapeStringWithWildcards(@Nonnull String s) {
        return escapeString(s)
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public static @Nonnull String quoteEscapedString(@Nonnull String s) {
        return "'" + s + "'";
    }

    @Override
    public String toString() {
        return quoted;
    }
}
