package io.github.sinri.Keel.servant;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeelCronParser {
    Set<Integer> minuteOptions = new HashSet<>();
    Set<Integer> hourOptions = new HashSet<>();
    Set<Integer> dayOptions = new HashSet<>();
    Set<Integer> monthOptions = new HashSet<>();
    Set<Integer> weekdayOptions = new HashSet<>();

    public KeelCronParser(String rawCronExpression) {
        String[] parts = rawCronExpression.trim().split("\\s+");
        if (parts.length != 5) {
            throw new RuntimeException("Invalid Cron Expression");
        }

        String minuteExpression = parts[0]; // 0-59
        String hourExpression = parts[1]; // 0-23
        String dayExpression = parts[2]; // 1-31
        String monthExpression = parts[3]; // 1-12
        String weekdayExpression = parts[4];// 0-6

        parseField(minuteExpression, minuteOptions, 0, 59);
        parseField(hourExpression, hourOptions, 0, 23);
        parseField(dayExpression, dayOptions, 1, 31);
        parseField(monthExpression, monthOptions, 1, 12);
        parseField(weekdayExpression, weekdayOptions, 0, 6);
    }

    private void parseField(String rawComponent, Set<Integer> optionSet, int min, int max) {
        if (rawComponent.equals("*")) {
            for (int i = min; i <= max; i++) {
                optionSet.add(i);
            }
            return;
        }

        ArrayList<String> parts = new ArrayList<>();
        if (rawComponent.contains(",")) {
            String[] t1 = rawComponent.split(",");
            parts.addAll(Arrays.asList(t1));
        } else {
            parts.add(rawComponent);
        }

        for (String part : parts) {
            part = part.trim();

            Matcher matcher1 = Pattern.compile("^(\\d+)-(\\d+)$").matcher(part);
            if (matcher1.matches()) {
                int start = Integer.parseInt(matcher1.group(1));
                int end = Integer.parseInt(matcher1.group(2));
                if (start < min || end > max || start > end) {
                    throw new IllegalArgumentException();
                }
                for (int i = start; i <= end; i++) {
                    optionSet.add(i);
                }
                continue;
            }

            Matcher matcher2 = Pattern.compile("^\\*[*/](\\d+)$").matcher(part);
            if (matcher2.matches()) {
                int mask = Integer.parseInt(matcher2.group(1));
                for (int i = 0; i <= max; i += mask) {
                    if (i >= min) {
                        optionSet.add(i);
                    }
                }
                continue;
            }

            throw new IllegalArgumentException();
        }
    }

    public boolean match(Calendar currentCalendar) {
        // currentCalendar := Calendar.getInstance();
        int minute = currentCalendar.get(Calendar.MINUTE);
        int hour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);
        int month = 1 + currentCalendar.get(Calendar.MONTH);// make JAN 1, ...
        int weekday = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1; // make sunday 0, ...

        return minuteOptions.contains(minute)
                && hourOptions.contains(hour)
                && dayOptions.contains(day)
                && monthOptions.contains(month)
                && weekdayOptions.contains(weekday);
    }
}
