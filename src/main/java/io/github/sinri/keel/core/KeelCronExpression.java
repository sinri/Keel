package io.github.sinri.keel.core;


import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * since 2.9.3 moved from io.github.sinri.keel.servant.sundial to here.
 */
public class KeelCronExpression {
    final Set<Integer> minuteOptions = new HashSet<>();
    final Set<Integer> hourOptions = new HashSet<>();
    final Set<Integer> dayOptions = new HashSet<>();
    final Set<Integer> monthOptions = new HashSet<>();
    final Set<Integer> weekdayOptions = new HashSet<>();
    private final @NotNull String rawCronExpression;

    public KeelCronExpression(@NotNull String rawCronExpression) {
        this.rawCronExpression = rawCronExpression;

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

    /**
     * @since 3.2.4
     */
    public static ParsedCalenderElements parseCalenderToElements(@NotNull Calendar currentCalendar) {
        return new ParsedCalenderElements(currentCalendar);
    }

    public boolean match(@NotNull Calendar currentCalendar) {
        ParsedCalenderElements parsedCalenderElements = new ParsedCalenderElements(currentCalendar);
        return match(parsedCalenderElements);
    }

    public boolean match(@NotNull ParsedCalenderElements parsedCalenderElements) {
        return minuteOptions.contains(parsedCalenderElements.minute)
                && hourOptions.contains(parsedCalenderElements.hour)
                && dayOptions.contains(parsedCalenderElements.day)
                && monthOptions.contains(parsedCalenderElements.month)
                && weekdayOptions.contains(parsedCalenderElements.weekday);
    }

    private void parseField(@NotNull String rawComponent, @NotNull Set<Integer> optionSet, int min, int max) {
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

            Matcher matcher0 = Pattern.compile("^\\d+$").matcher(part);
            if (matcher0.matches()) {
                optionSet.add(Integer.parseInt(part));
                continue;
            }

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

    @NotNull
    public String getRawCronExpression() {
        return rawCronExpression;
    }

    /**
     * @since 3.2.4
     */
    public static class ParsedCalenderElements {
        public final int minute;
        public final int hour;
        public final int day;
        public final int month;
        public final int weekday;

        // debug use
        public final int second;

        public ParsedCalenderElements(@NotNull Calendar currentCalendar) {
            minute = currentCalendar.get(Calendar.MINUTE);
            hour = currentCalendar.get(Calendar.HOUR_OF_DAY);
            day = currentCalendar.get(Calendar.DAY_OF_MONTH);
            month = 1 + currentCalendar.get(Calendar.MONTH);// make JAN 1, ...
            weekday = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1; // make sunday 0, ...
            second = currentCalendar.get(Calendar.SECOND);
        }

        @Override
        public String toString() {
            return "(" + second + ") " + minute + " " + hour + " " + day + " " + month + " " + weekday;
        }
    }

    @Override
    public String toString() {
        return getRawCronExpression();
    }
}
