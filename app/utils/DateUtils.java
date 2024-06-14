package utils;

import constants.Config;
import constants.Constants;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DateUtils - a utility class for date formatting and retrieval
 */
public class DateUtils {
    private static final DateUtils INSTANCE = new DateUtils();
    public static final ZoneId ZONE_ID = ZoneId.of(Config.TIMEZONE);
    public static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_INPUT_FORMAT).withZone(ZONE_ID);
    public static final DateTimeFormatter DATE_TIME_OUTPUT_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_TIME_OUTPUT_FORMAT).withZone(ZONE_ID);
    public static final DateTimeFormatter DATE_OUTPUT_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_OUTPUT_FORMAT).withZone(ZONE_ID);
    public static final DateTimeFormatter TIME_OUTPUT_FORMATTER = DateTimeFormatter.ofPattern(Constants.TIME_OUTPUT_FORMAT).withZone(ZONE_ID);
    public static final DateTimeFormatter DATE_NO_DASH_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_NO_DASH).withZone(ZONE_ID);
    public static final DateTimeFormatter DATE_TIME_NO_DASH_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT_NO_DASH).withZone(ZONE_ID);
    public static final DateTimeFormatter GIT_TIME_FORMATTER = DateTimeFormatter.ofPattern(Constants.GIT_TIME_FORMAT);

    /**
     * Private constructor
     */
    private DateUtils() {
    }

    public static DateUtils getInstance() {
        return INSTANCE;
    }

    /**
     * Get the current date in epoch milliseconds
     *
     * @return date in epoch milliseconds
     */
    public static long getCurrentEpochTime() {
        return getCurrentZonedDateTime().toInstant().toEpochMilli();
    }

    /**
     * Get the current date and time in readable format
     *
     * @return date in date time output format
     */
    public static String getCurrentDateTimeString() {
        return DATE_TIME_OUTPUT_FORMATTER.format(getCurrentZonedDateTime());
    }

    public static ZonedDateTime getCurrentZonedDateTime() {
        return ZonedDateTime.now(ZONE_ID);
    }
}
