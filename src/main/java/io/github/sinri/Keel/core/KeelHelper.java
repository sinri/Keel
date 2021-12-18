package io.github.sinri.Keel.core;

import java.util.List;

public class KeelHelper {
    public static String joinStringArray(List<?> x, String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < x.size(); i++) {
            if (i > 0) result.append(separator);
            result.append(x.get(i).toString());
        }
        return result.toString();
    }
}
