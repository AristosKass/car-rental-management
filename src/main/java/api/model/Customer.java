package api.model;

/**
 * <h2>Customer (Πελάτης)</h2>
 * Πελάτης που μπορεί να πραγματοποιεί ενοικιάσεις.
 * <p>
 * Μοναδικό αναγνωριστικό: {@code ΑΦΜ}.
 */
public class Customer extends Person {

    /** ΑΦΜ (μοναδικό). */
    private String afm;

    /**
     * Δημιουργεί νέο πελάτη.
     *
     * @param afm      ΑΦΜ (μοναδικό)
     * @param fullName ονοματεπώνυμο
     * @param phone    τηλέφωνο
     * @param email    email
     */
    public Customer(String afm, String fullName, String phone, String email) {
        super(fullName, phone, email);
        this.afm = afm;
    }

    /**
     * @return το ΑΦΜ
     */
    public String getAfm() {
        return afm;
    }

    /**
     * @param afm το ΑΦΜ
     */
    public void setAfm(String afm) {
        this.afm = afm;
    }
}
