package reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/*
  Small collection of static validation helpers so form classes stay
  focused on layout/wiring rather than parsing logic.
 */
public final class ValidationUtils {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PATTERN);

    private ValidationUtils() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isNumeric(String s) {
        if (isBlank(s)) {
            return false;
        }
        return s.trim().chars().allMatch(Character::isDigit);
    }

    /** Strict validation: must actually parse as a real calendar date in yyyy-MM-dd. */
    public static boolean isValidDate(String s) {
        if (isBlank(s)) {
            return false;
        }
        try {
            LocalDate.parse(s.trim(), DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
