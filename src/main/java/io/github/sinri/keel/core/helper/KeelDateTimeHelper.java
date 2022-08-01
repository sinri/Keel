package io.github.sinri.keel.core.helper;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @since 2.6
 */
public class KeelDateTimeHelper {
    private static final KeelDateTimeHelper instance = new KeelDateTimeHelper();

    private KeelDateTimeHelper() {

    }

    public static KeelDateTimeHelper getInstance() {
        return instance;
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
}
