package api.model;

/**
 * <h2>Employee (Χρήστης/Υπάλληλος)</h2>
 * Αναπαριστά έναν χρήστη που μπορεί να συνδεθεί στο σύστημα.
 * <p>
 * Για τις ανάγκες της εργασίας, ο κωδικός αποθηκεύεται ως απλό κείμενο στο CSV.
 * Σε πραγματική εφαρμογή θα χρειαζόταν hashing/salting.
 */
public class Employee extends Person {

    /** Μοναδικό username (login id). */
    private String username;

    /** Password... (plain text - coursework). */
    private String password;

    /**
     * Δημιουργεί νέο employee.
     *
     * @param name     όνομα (μέρος του ονοματεπωνύμου)
     * @param surname  επώνυμο (μέρος του ονοματεπωνύμου)
     * @param username μοναδικό username
     * @param email    email
     * @param password password (plain text)
     */
    public Employee(String name, String surname, String username, String email, String password) {
        super((name == null ? "" : name.trim()) + " " + (surname == null ? "" : surname.trim()), "", email);
        this.username = username;
        this.password = password;
    }

    /**
     * @return το μοναδικό username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username το μοναδικό username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return το password (plain text - για την εργασία)
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password το password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return το όνομα (1ο μέρος του {@link #getFullName()})
     */
    public String getName() {
        String[] parts = getFullName().split(" ", 2);
        return parts.length > 0 ? parts[0] : "";
    }

    /**
     * @return το επώνυμο (2ο μέρος του {@link #getFullName()})
     */
    public String getSurname() {
        String[] parts = getFullName().split(" ", 2);
        return parts.length == 2 ? parts[1] : "";
    }
}
