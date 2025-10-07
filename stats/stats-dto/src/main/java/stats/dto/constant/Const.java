package stats.dto.constant;

import java.time.format.DateTimeFormatter;

public class Const {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_TIME_WITH_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
}
