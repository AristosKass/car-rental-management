package api.util;

import api.exception.ValidationException;

/**
 * <h2>Validators</h2>
 * Συγκεντρώνει βασικούς ελέγχους εγκυρότητας για να αποφεύγεται η επανάληψη κώδικα.
 */
public final class Validators {

    private Validators() {
        // utility class
    }

    /**
     * Απαιτεί τιμή μη κενή.
     *
     * @param value   τιμή
     * @param message μήνυμα εξαίρεσης
     * @throws ValidationException αν {@code value} είναι null/blank
     */
    public static void requireNotBlank(String value, String message) {
        if (StringUtil.isBlank(value)) throw new ValidationException(message);
    }

    /**
     * Απαιτεί θετικό ακέραιο.
     *
     * @param value   τιμή
     * @param message μήνυμα εξαίρεσης
     * @throws ValidationException αν {@code value<=0}
     */
    public static void requirePositive(int value, String message) {
        if (value <= 0) throw new ValidationException(message);
    }

    /**
     * Έλεγχος «λογικού» έτους κατασκευής.
     * <p>
     * Για τις ανάγκες της εργασίας θεωρούμε έγκυρα τα έτη [1950, 2100].
     *
     * @param year έτος
     */
    public static void requireYearReasonable(int year) {
        if (year < 1950 || year > 2100) throw new ValidationException("Μη έγκυρο έτος κατασκευής.");
    }

    /**
     * Έλεγχος ΑΦΜ (9 ψηφία).
     *
     * @param afm ΑΦΜ
     */
    public static void requireAfm(String afm) {
        requireNotBlank(afm, "Το ΑΦΜ είναι υποχρεωτικό.");
        String a = afm.trim();
        if (!a.matches("\\d{9}")) {
            throw new ValidationException("Το ΑΦΜ πρέπει να έχει 9 ψηφία.");
        }
    }
}
