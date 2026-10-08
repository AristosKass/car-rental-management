package api.exception;

/**
 * Εξαίρεση αυθεντικοποίησης.
 * <p>
 * Χρησιμοποιείται όταν αποτυγχάνει login (λάθος username/password).
 */
public class AuthException extends RuntimeException {

    /**
     * @param message περιγραφή σφάλματος
     */
    public AuthException(String message) {
        super(message);
    }
}
