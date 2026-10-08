package api.model;

/**
 * <h2>Βασική κλάση για πρόσωπα</h2>
 * Αποτελεί κοινή βάση για οντότητες που έχουν στοιχεία επικοινωνίας.
 * <p>
 * Χρησιμοποιείται από:
 * <ul>
 *   <li>{@link Customer}: πελάτες</li>
 *   <li>{@link Employee}: χρήστες/υπάλληλοι</li>
 * </ul>
 */
public abstract class Person {

    /** Ονοματεπώνυμο (π.χ. "Γιώργος Παπαδόπουλος"). */
    private String fullName;

    /** Τηλέφωνο επικοινωνίας (μπορεί να είναι κενό). */
    private String phone;

    /** Email επικοινωνίας (μπορεί να είναι κενό). */
    private String email;

    /**
     * Constructor βάσης.
     *
     * @param fullName ονοματεπώνυμο
     * @param phone    τηλέφωνο (προαιρετικό)
     * @param email    email (προαιρετικό)
     */
    protected Person(String fullName, String phone, String email) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    /**
     * @return το ονοματεπώνυμο
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName το ονοματεπώνυμο
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return το τηλέφωνο (μπορεί να είναι κενό)
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone το τηλέφωνο
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return το email (μπορεί να είναι κενό)
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email το email
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
