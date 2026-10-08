package api.exception;

/**
 * Εξαίρεση «δεν βρέθηκε».
 * <p>
 * Χρησιμοποιείται όταν μια οντότητα δεν υπάρχει (π.χ. αναζήτηση οχήματος με άγνωστο id).
 */
public class NotFoundException extends RuntimeException {

    /**
     * @param message περιγραφή σφάλματος
     */
    public NotFoundException(String message) {
        super(message);
    }
}
