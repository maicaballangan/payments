package interfaces;

import constants.Config;
import net.andreinc.mockneat.MockNeat;
import net.andreinc.mockneat.types.enums.PassStrengthType;
import net.andreinc.mockneat.types.enums.StringFormatType;
import net.andreinc.mockneat.types.enums.StringType;
import net.andreinc.mockneat.types.enums.UserNameType;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;

/**
 * Mocking resources for testing purposes
 *
 * @author Maica Ballangan
 * @since v1
 */
public interface Mockable {
    String DEFAULT_LANGUAGE = "English (United States)";
    String MOCK_USER = "mockable@gmail.com";
    String DEFAULT_COUNTRY = Config.COUNTRY;
    LocalDate MAX_DATE = LocalDate.of(LocalDate.now().getYear() + 10, 12, 31);
    Random random = new Random();

    /**
     * Class used for mocking
     */
    MockNeat MOCK = MockNeat.threadLocal();

    static String words(final int min, final int max) {
        return MOCK.words().accumulate(MOCK.ints().range(min, max).val(), " ").val();
    }

    static String properNouns() {
        return MOCK.words().nouns().accumulate(MOCK.ints().range(1, 3).val(), " ").format(StringFormatType.CAPITALIZED).val();
    }

    static String words() {
        return MOCK.words().val();
    }

    static String names() {
        return MOCK.names().full().val();
    }

    static String namesNullable() {
        if (Boolean.TRUE.equals(MOCK.bools().probability(70d).val())) {
            return names();
        } else {
            return null;
        }
    }

    static String passwords() {
        return MOCK.passwords().type(PassStrengthType.MEDIUM).val();
    }

    static String mobiles() {
        return MOCK.regex("([+]\\d{2})\\d{10}").val();
    }

    static String usernames() {
        return StringUtils.rightPad(MOCK.users().type(UserNameType.ADJECTIVE_NOUN).val(), 8, chars());
    }

    static String titles() {
        if (Boolean.TRUE.equals(MOCK.bools().probability(50d).val())) {
            return MOCK.fmt("#{adj} #{noun}")
                    .param("adj", MOCK.words().adjectives().format(StringFormatType.CAPITALIZED))
                    .param("noun", MOCK.words().nouns().format(StringFormatType.CAPITALIZED))
                    .valStr();
        } else {
            return MOCK.fmt("#{verb} #{adj} #{noun}")
                    .param("verb", MOCK.words().verbs().format(StringFormatType.CAPITALIZED))
                    .param("adj", MOCK.words().adjectives().format(StringFormatType.CAPITALIZED))
                    .param("noun", MOCK.words().nouns().format(StringFormatType.CAPITALIZED))
                    .valStr();
        }
    }

    static String wordsShort() {
        return MOCK.words().accumulate(MOCK.ints().range(1, 5).val(), " ").format(StringFormatType.CAPITALIZED).val();
    }

    static String wordsMedium() {
        return MOCK.words().accumulate(MOCK.ints().range(10, 25).val(), " ").format(StringFormatType.CAPITALIZED).val();
    }

    static String wordsLong() {
        return MOCK.words().accumulate(MOCK.ints().range(50, 100).val(), " ").format(StringFormatType.CAPITALIZED).val();
    }

    static String wordsSuperLong() {
        return MOCK.words().accumulate(MOCK.ints().range(100, 300).val(), " ").format(StringFormatType.CAPITALIZED).val();
    }

    static String wordsShortNullable() {
        if (Boolean.TRUE.equals(MOCK.bools().probability(50d).val())) {
            return MOCK.words().accumulate(MOCK.ints().range(1, 5).val(), " ").format(StringFormatType.CAPITALIZED).val();
        } else {
            return null;
        }
    }

    static String wordsMediumNullable() {
        if (Boolean.TRUE.equals(MOCK.bools().probability(70d).val())) {
            return MOCK.words().accumulate(MOCK.ints().range(10, 25).val(), " ").format(StringFormatType.CAPITALIZED).val();
        } else {
            return null;
        }
    }

    static String wordsLongNullable() {
        if (Boolean.TRUE.equals(MOCK.bools().probability(70d).val())) {
            return MOCK.words().accumulate(MOCK.ints().range(50, 100).val(), " ").format(StringFormatType.CAPITALIZED).val();
        } else {
            return null;
        }
    }

    static char chars() {
        return MOCK.chars().val();
    }

    static String stringUpper(final int size) {
        return MOCK.strings().size(size).type(StringType.LETTERS).format(StringFormatType.UPPER_CASE).val();
    }

    static String stringAlphaNumUpper(final int size) {
        return MOCK.strings().size(size).type(StringType.ALPHA_NUMERIC).format(StringFormatType.UPPER_CASE).val();
    }

    static String stringNum(final int size) {
        return MOCK.strings().size(size).type(StringType.NUMBERS).val();
    }

    static BigDecimal prices(final int min, final int max) {
        return BigDecimal.valueOf(MOCK.doubles().range(min, max).val()).setScale(2, RoundingMode.HALF_UP);
    }

    static double ratings() {
        return BigDecimal.valueOf(MOCK.doubles().range(1, 5).val()).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    static double doubles(final int min, final int max) {
        return MOCK.doubles().range(min, max).val();
    }

    static <U extends Enum<?>> U from(final Class<U> clazz) {
        return MOCK.from(clazz).val();
    }

    static int ints(final int min, final int max) {
        return MOCK.ints().range(min, max).val();
    }

    static String places() {
        return MOCK.cities().capitals().format(StringFormatType.CAPITALIZED).val() + ", " + MOCK.cities().capitals().format(StringFormatType.CAPITALIZED).val() + ", " + MOCK.countries().names().format(StringFormatType.CAPITALIZED).val();
    }

    static String cities() {
        return MOCK.cities().capitals().format(StringFormatType.CAPITALIZED).val();
    }

    static String states() {
        return MOCK.cities().capitals().format(StringFormatType.CAPITALIZED).val();
    }

    static String currencies() {
        return MOCK.currencies().code().format(StringFormatType.UPPER_CASE).val();
    }

    static String languages() {
        return DEFAULT_LANGUAGE;
    }

    static String countryCodes() {
        return MOCK.countries().iso2().format(StringFormatType.UPPER_CASE).val();
    }

    static String countries() {
        return MOCK.countries().names().format(StringFormatType.CAPITALIZED).val();
    }

    static boolean bools() {
        return MOCK.bools().val();
    }

    static String emails() {
        return MOCK.emails().val();
    }

    static String emailsNullable() {
        if (Boolean.TRUE.equals(MOCK.bools().probability(70d).val())) {
            return MOCK.emails().val();
        } else {
            return null;
        }
    }

    static String stringDates() {
        return MOCK.localDates().valStr();
    }

    static LocalDate dates() {
        return MOCK.localDates().val();
    }

    static LocalDate datesThisYear() {
        return MOCK.localDates().thisYear().val();
    }

    static LocalDate datesThisMonth() {
        return MOCK.localDates().thisMonth().val();
    }

    static LocalDate datesFuture() {
        return MOCK.localDates().future(MAX_DATE).val();
    }

    static LocalDate datesPast() {
        return MOCK.localDates().past(LocalDate.MIN).val();
    }

    static String domains() {
        return MOCK.domains().format(StringFormatType.CAPITALIZED).val();
    }

    static String urls() {
        return MOCK.urls().val();
    }
}