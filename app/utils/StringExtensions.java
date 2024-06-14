package utils;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import play.templates.JavaExtensions;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * StringExtensions
 * <p>
 * Various methods to manipulate Strings
 */
public class StringExtensions extends JavaExtensions {

    private static final int OFFSET = 6;

    public static String rightHTMLPad(String value, int size) {
        return StringUtils.rightPad(value, (size - value.length()) * OFFSET + value.length(), "&nbsp;");
    }

    public static String toDecimalPlaces(String value, int places) {
        return StringUtils.isBlank(value) ? null : new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public static BigDecimal toDecimalPlaces(BigDecimal value, int places) {
        return (value == null) ? null : value.setScale(places, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    public static BigDecimal toBigDecimal(String value, int places) {
        return StringUtils.isBlank(value) ? null : new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    public static String toPercent(BigDecimal value) {
        return String.format("%,.2f%s", value.multiply(BigDecimal.valueOf(100L)), "%");
    }

    public static BigDecimal fromPercent(BigDecimal value) {
        return value.divide(BigDecimal.valueOf(100L));
    }

    public static boolean isWithinRange(String value, String original, String pct) {
        if (StringUtils.isBlank(value) || StringUtils.isBlank(original) || StringUtils.isBlank(pct)) {
            return true;
        }
        BigDecimal delta = new BigDecimal(original).multiply(fromPercent(new BigDecimal(pct)));
        return new BigDecimal(value).compareTo(new BigDecimal(original).add(delta)) <= 0 && new BigDecimal(value).compareTo(new BigDecimal(original).subtract(delta)) >= 0;
    }

    public static boolean isZero(String value, String original) {
        return (StringUtils.isNotBlank(original) && "0".equals(original))
            || (StringUtils.isNotBlank(value) && "0".equals(value));
    }

    public static int getNumberOfDecimalPlaces(String value) {
        String string = new BigDecimal(value).toPlainString();
        int index = string.indexOf('.');
        return index < 0 ? 0 : string.length() - index - 1;
    }

    public static int getNumberOfDecimalPlaces(BigDecimal value) {
        String string = value.toPlainString();
        int index = string.indexOf('.');
        return index < 0 ? 0 : string.length() - index - 1;
    }

    public static String formatCcy(BigDecimal value) {
        return String.format("%,.2f", value);
    }

    public static String formatCcy(String value) {
        return String.format("%,.2f", new BigDecimal(value));
    }

    public static String upperSnaketoCamelCase(final String text) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, text);
    }

    public static String snakeToUpperCamelCase(final String text) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, text);
    }

    public static String snakeToTitleCase(final String text) {
        return WordUtils.capitalizeFully(text, new char[]{'_'}).replaceAll("_", " ");
    }

    public static String snakeToLowerCase(final String text) {
        return WordUtils.uncapitalize(text, new char[]{'_'}).replaceAll("_", " ");
    }

    public static String upperCamelToSnakeCase(final String text) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, text);
    }

    public static String upperCamelToUpperSnake(final String text) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, text);
    }

    public static String upperCamelToLowerCamel(final String text) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, text);
    }
}