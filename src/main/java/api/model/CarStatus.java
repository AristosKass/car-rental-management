package api.model;

/**
 * Κατάσταση διαθεσιμότητας οχήματος.
 * <p>
 * Η τιμή αυτή αποθηκεύεται και στο CSV ως ελληνικό label (π.χ. "Διαθέσιμο").
 */
public enum CarStatus {

    /** Το όχημα είναι διαθέσιμο για ενοικίαση. */
    AVAILABLE("Διαθέσιμο"),

    /** Το όχημα είναι ήδη ενοικιασμένο. */
    RENTED("Ενοικιασμένο");

    private final String greekLabel;

    CarStatus(String greekLabel) {
        this.greekLabel = greekLabel;
    }

    /**
     * @return το ελληνικό label που χρησιμοποιείται στο CSV
     */
    public String getGreekLabel() {
        return greekLabel;
    }

    /**
     * Μετατρέπει την τιμή του CSV σε {@link CarStatus}.
     * Δέχεται τόσο ελληνικές τιμές όσο και απλές αγγλικές ("available", "rented").
     *
     * @param value κείμενο από το CSV
     * @return status (default: {@link #AVAILABLE} αν δεν αναγνωρίζεται)
     */
    public static CarStatus fromCsv(String value) {
        if (value == null) return AVAILABLE;
        String v = value.trim();
        for (CarStatus s : values()) {
            if (s.greekLabel.equalsIgnoreCase(v)) return s;
        }
        // Accept some common alternatives
        if ("available".equalsIgnoreCase(v)) return AVAILABLE;
        if ("rented".equalsIgnoreCase(v)) return RENTED;
        return AVAILABLE;
    }
}
