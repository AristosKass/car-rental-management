package api.exception;

/**
 * Εξαίρεση validation.
 * <p>
 * Πετιέται όταν δεν ικανοποιούνται προϋποθέσεις εισόδου/δεδομένων.
 * Παράδειγμα: κενό ΑΦΜ, λάθος format ημερομηνίας, μη-διαθέσιμο όχημα για ενοικίαση κ.λπ.
 */
public class ValidationException extends RuntimeException {

    /**
     * @param message περιγραφή σφάλματος
     */
    public ValidationException(String message) {
        super(message);
    }
}
