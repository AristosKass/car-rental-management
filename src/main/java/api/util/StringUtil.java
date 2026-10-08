package api.util;

/**
 * Βοηθητικές μέθοδοι για {@link String}.
 */
public final class StringUtil {

    private StringUtil() {
        // utility class
    }

    /**
     * Ελέγχει αν ένα string είναι null ή περιέχει μόνο whitespace.
     *
     * @param s string
     * @return true αν είναι κενό/blank
     */
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Null-safe trim.
     *
     * @param s string
     * @return κενό string αν {@code s==null}, αλλιώς {@code s.trim()}
     */
    public static String nvl(String s) {
        return s == null ? "" : s.trim();
    }
}
