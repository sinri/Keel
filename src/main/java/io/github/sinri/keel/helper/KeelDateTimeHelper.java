package io.github.sinri.keel.helper;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * @since 2.6
 */
public class KeelDateTimeHelper {
    private static final KeelDateTimeHelper instance = new KeelDateTimeHelper();

    private KeelDateTimeHelper() {

    }

    static KeelDateTimeHelper getInstance() {
        return instance;
    }

    /**
     * @return current timestamp expressed in MySQL Date Time Format
     * @since 3.0.0
     */
    public String getCurrentDateExpression() {
        return getCurrentDateExpression("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * @param format "yyyyMMdd" or "yyyy-MM-dd HH:mm:ss", etc. if null, return null
     * @return the date string or null
     * @since 2.6
     */
    public String getCurrentDateExpression(String format) {
        Date currentTime = new Date();
        return getDateExpression(currentTime, format);
    }

    /**
     * @param format for example: yyyy-MM-ddTHH:mm:ss
     * @since 2.6
     */
    public String getDateExpression(Date date, String format) {
        if (format == null || format.isEmpty()) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    /**
     * From MySQL DataTime String to Standard Expression
     *
     * @param localDateTimeExpression yyyy-MM-ddTHH:mm:ss
     * @return yyyy-MM-dd HH:mm:ss
     * @since 2.7
     */
    public String getMySQLFormatLocalDateTimeExpression(String localDateTimeExpression) {
        return LocalDateTime.parse(localDateTimeExpression)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * @return Date Time in RFC 1123: Mon, 31 Oct 2022 01:18:43 GMT
     * @since 2.9.1
     */
    public String getGMTDateTimeExpression(ZoneId zoneId) {
        DateTimeFormatter gmt = DateTimeFormatter.ofPattern(
                        "EEE, dd MMM yyyy HH:mm:ss z",
                        Locale.ENGLISH
                )
                .withZone(ZoneId.of("GMT"));
        return gmt.format(LocalDateTime.now(zoneId));
    }

    /**
     * @since 3.0.1
     */
    public String getGMTDateTimeExpression() {
        return getGMTDateTimeExpression(ZoneId.systemDefault());
    }

    /**
     * @since 3.0.1
     */
    protected String makeStandardWidthField(int x, int w) {
        StringBuilder s = new StringBuilder(String.valueOf(x));
        if (s.length() < w) {
            for (int i = 0; i < w - s.length(); i++) {
                s.insert(0, "0");
            }
        }
        return String.valueOf(s);
    }

    /**
     * @since 3.0.1
     */
    public String toMySQLDatetime(LocalDateTime datetime) {
        return makeStandardWidthField(datetime.getYear(), 4)
                + "-" + makeStandardWidthField(datetime.getMonthValue(), 2)
                + "-" + makeStandardWidthField(datetime.getDayOfMonth(), 2)
                + " "
                + makeStandardWidthField(datetime.getHour(), 2)
                + ":" + makeStandardWidthField(datetime.getMinute(), 2)
                + ":" + makeStandardWidthField(datetime.getSecond(), 2);
    }

    public static final String MYSQL_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String MYSQL_DATE_PATTERN = "yyyy-MM-dd";
    public static final String MYSQL_TIME_PATTERN = "HH:mm:ss";

    /**
     * @param formatPattern MYSQL_DATETIME_PATTERN,MYSQL_DATE_PATTERN,MYSQL_TIME_PATTERN
     * @since 3.0.1
     */
    public String toMySQLDatetime(LocalDateTime localDateTime, String formatPattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatPattern);
        return localDateTime.format(dateTimeFormatter);
    }
}
