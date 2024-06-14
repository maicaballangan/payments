package constants;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    // Path constants
    public static final char DELIMITER_PATH = '/';
    public static final char DELIMITER_DOT = '.';
    public static final char DELIMITER_DASH = '-';
    public static final char DELIMITER_SPACE = ' ';
    public static final char DELIMITER_UNDERSCORE = '_';
    public static final String DELIMITER_COMMA = ",";

    // Default date formats
    public static final String DATE_INPUT_FORMAT = "yyyy-MM-dd";
    public static final String MILITARY_TIME_FORMAT = "HH:mm";
    public static final String DATE_OUTPUT_FORMAT = "MMM d, yyyy";
    public static final String TIME_OUTPUT_FORMAT = "h:mm a";
    public static final String TIME_OUTPUT_FULL_FORMAT = "h:mm:ss a";
    public static final String DATE_TIME_OUTPUT_FORMAT = DATE_OUTPUT_FORMAT + " " + TIME_OUTPUT_FULL_FORMAT + " z";
    public static final String DATE_FORMAT_NO_DASH = "yyyyMMdd";
    public static final String DATE_TIME_FORMAT_NO_DASH = "yyyyMMddHHmmss";
    public static final String GIT_TIME_FORMAT = "EEE MMM d HH:mm:ss yyyy Z";

    public static final String CURRENCY_FORMAT = "[A-Z]{3}";
    public static final long RESULTS_SIZE = 10L;
}
