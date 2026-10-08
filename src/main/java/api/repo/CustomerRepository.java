package api.repo;

import api.exception.NotFoundException;
import api.exception.ValidationException;
import api.model.Customer;
import api.util.AppFiles;
import api.util.CsvUtil;
import api.util.StringUtil;
import api.util.Validators;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <h2>CustomerRepository</h2>
 * Repository πελατών με αποθήκευση σε CSV.
 * <p>
 * <b>CSV file:</b> {@code data/customers.csv} (βλ. {@link AppFiles#customersFile()}).
 * <p>
 * <b>CSV header:</b>
 * <pre>
 * ΑΦΜ,ονοματεπώνυμο,τηλέφωνο,email
 * </pre>
 * <p>
 * Το repository φορτώνει όλα τα δεδομένα στη μνήμη στην εκκίνηση ({@link #reload()}) και
 * σε κάθε add/update ξαναγράφει ολόκληρο το CSV.
 */
public class CustomerRepository {

    private final File file;
    private final List<Customer> customers = new ArrayList<>();

    /**
     * Δημιουργεί repository και φορτώνει από το CSV.
     */
    public CustomerRepository() {
        this.file = AppFiles.customersFile();
        reload();
    }

    /**
     * Επαναφορτώνει τα δεδομένα από το αρχείο. Χρήσιμο σε tests ή όταν θέλουμε refresh.
     *
     * @throws RuntimeException αν αποτύχει η ανάγνωση
     */
    public final void reload() {
        customers.clear();
        try {
            List<List<String>> rows = CsvUtil.readAll(file);
            if (rows.isEmpty()) return;

            // Header: ΑΦΜ,ονοματεπώνυμο,τηλέφωνο,email
            for (int i = 1; i < rows.size(); i++) {
                List<String> r = rows.get(i);
                if (r.size() < 4) continue;
                customers.add(new Customer(r.get(0), r.get(1), r.get(2), r.get(3)));
            }
        } catch (IOException e) {
            throw new RuntimeException("Σφάλμα ανάγνωσης αρχείου πελατών: " + e.getMessage(), e);
        }
    }

    /**
     * @return unmodifiable προβολή όλων των πελατών (η εσωτερική λίστα δεν πρέπει να αλλάζει εκτός repository)
     */
    public List<Customer> findAll() {
        return Collections.unmodifiableList(customers);
    }

    /**
     * Αναζήτηση πελάτη με ΑΦΜ.
     *
     * @param afm ΑΦΜ
     * @return πελάτης ή {@code null} αν δεν βρέθηκε
     */
    public Customer findByAfm(String afm) {
        String a = StringUtil.nvl(afm);
        for (Customer c : customers) {
            if (StringUtil.nvl(c.getAfm()).equalsIgnoreCase(a)) return c;
        }
        return null;
    }

    /**
     * Προσθήκη νέου πελάτη.
     *
     * @param customer πελάτης
     * @throws ValidationException αν αποτύχει validation ή αν υπάρχει ήδη ίδιο ΑΦΜ
     */
    public void add(Customer customer) {
        validateCustomer(customer, null);
        customers.add(customer);
        save();
    }

    /**
     * Ενημέρωση υπάρχοντος πελάτη (με βάση το ΑΦΜ).
     *
     * @param customer ενημερωμένα στοιχεία
     * @throws NotFoundException   αν δεν υπάρχει πελάτης με αυτό το ΑΦΜ
     * @throws ValidationException αν αποτύχει validation
     */
    public void update(Customer customer) {
        Customer existing = findByAfm(customer.getAfm());
        if (existing == null) throw new NotFoundException("Ο πελάτης δεν βρέθηκε (ΑΦΜ=" + customer.getAfm() + ")");
        validateCustomer(customer, existing);
        existing.setFullName(customer.getFullName());
        existing.setPhone(customer.getPhone());
        existing.setEmail(customer.getEmail());
        save();
    }

    /**
     * Έλεγχοι εγκυρότητας. Το {@code ignore} χρησιμοποιείται σε update για να μην θεωρήσουμε «διπλό» το ίδιο record.
     */
    private void validateCustomer(Customer c, Customer ignore) {
        if (c == null) throw new ValidationException("Κενός πελάτης.");
        Validators.requireAfm(c.getAfm());
        Validators.requireNotBlank(c.getFullName(), "Το ονοματεπώνυμο είναι υποχρεωτικό.");
        Validators.requireNotBlank(c.getPhone(), "Το τηλέφωνο είναι υποχρεωτικό.");
        Validators.requireNotBlank(c.getEmail(), "Το email είναι υποχρεωτικό.");

        for (Customer other : customers) {
            if (ignore != null && other == ignore) continue;
            if (StringUtil.nvl(other.getAfm()).equalsIgnoreCase(StringUtil.nvl(c.getAfm()))) {
                throw new ValidationException("Υπάρχει ήδη πελάτης με αυτό το ΑΦΜ.");
            }
        }
    }

    /**
     * Γράφει ολόκληρη τη λίστα στο CSV.
     */
    private void save() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("ΑΦΜ", "ονοματεπώνυμο", "τηλέφωνο", "email"));
        for (Customer c : customers) {
            rows.add(Arrays.asList(c.getAfm(), c.getFullName(), c.getPhone(), c.getEmail()));
        }
        try {
            CsvUtil.writeAll(file, rows);
        } catch (IOException ex) {
            throw new RuntimeException("Σφάλμα αποθήκευσης αρχείου πελατών: " + ex.getMessage(), ex);
        }
    }
}
