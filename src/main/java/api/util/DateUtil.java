package api.util;

import api.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Βοηθητικές μέθοδοι για μετατροπές ημερομηνιών.
 * <p>
 * Η εφαρμογή χρησιμοποιεί ISO-8601 ημερομηνίες ({@code yyyy-MM-dd}) τόσο στο GUI
 * όσο και στα CSV αρχεία.
 */
public final class DateUtil {

    private DateUtil() {
        // utility class
    }

    /**
     * Parse ISO date (yyyy-MM-dd). Αν το κείμενο είναι κενό/blank, θεωρείται σφάλμα.
     *
     * @param text ημερομηνία ως κείμενο
     * @return {@link LocalDate}
     * @throws ValidationException αν δεν είναι έγκυρο ISO date
     */
    public static LocalDate parseIso(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Μη έγκυρη ημερομηνία. Χρησιμοποιήστε μορφή yyyy-MM-dd.");
        }
    }

    /**
     * Alias του {@link #parseIso(String)} (πιο σύντομο όνομα για repositories).
     */
    public static LocalDate parse(String text) {
        return parseIso(text);
    }

    /**
     * Parse ISO date, αλλά επιστρέφει {@code null} όταν το κείμενο είναι κενό.
     *
     * @param text κείμενο
     * @return ημερομηνία ή {@code null}
     */
    public static LocalDate parseNullable(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;
        return parseIso(t);
    }

    /**
     * Formats a date for CSV (ISO-8601).
     *
     * @param date ημερομηνία (μη-null)
     * @return ISO string
     */
    public static String formatIso(LocalDate date) {
        if (date == null) throw new ValidationException("Η ημερομηνία δεν μπορεί να είναι null.");
        return date.toString();
    }

    /**
     * Alias του {@link #formatIso(LocalDate)}.
     */
    public static String format(LocalDate date) {
        return formatIso(date);
    }

    /**
     * Format nullable ημερομηνίας (για optional πεδία όπως returnDate).
     *
     * @param date ημερομηνία ή {@code null}
     * @return ISO string ή κενό
     */
    public static String formatNullable(LocalDate date) {
        return date == null ? "" : date.toString();
    }
}
