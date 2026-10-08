package api.service;

import api.model.Customer;
import api.repo.CustomerRepository;
import api.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * <h2>CustomerService</h2>
 * Επιχειρησιακές λειτουργίες για πελάτες.
 * <p>
 * Τα πιο «βαριά» validations (ΑΦΜ κ.λπ.) γίνονται στο {@link CustomerRepository} ώστε τα δεδομένα να παραμένουν συνεπή.
 */
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * @param customerRepository repo πελατών
     */
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * @return όλοι οι πελάτες
     */
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    /**
     * @param afm ΑΦΜ
     * @return πελάτης ή {@code null}
     */
    public Customer findByAfm(String afm) {
        return customerRepository.findByAfm(afm);
    }

    /**
     * @param customer πελάτης
     */
    public void add(Customer customer) {
        customerRepository.add(customer);
    }

    /**
     * @param customer ενημερωμένα στοιχεία πελάτη (ίδιο ΑΦΜ)
     */
    public void update(Customer customer) {
        customerRepository.update(customer);
    }

    /**
     * Αναζήτηση πελατών με συνδυασμό κριτηρίων.
     *
     * @param afm      κριτήριο ΑΦΜ (αν είναι digits, γίνεται ακριβής αντιστοίχιση)
     * @param fullName κριτήριο ονοματεπώνυμου (contains)
     * @param phone    κριτήριο τηλεφώνου (αν είναι digits, γίνεται ακριβής αντιστοίχιση)
     * @return λίστα αποτελεσμάτων
     */
    public List<Customer> search(String afm, String fullName, String phone) {
        List<Customer> out = new ArrayList<>();
        for (Customer c : customerRepository.findAll()) {
            if (!matchesExactOrContains(StringUtil.nvl(afm), c.getAfm(), true)) continue;
            if (!matchesExactOrContains(StringUtil.nvl(fullName), c.getFullName(), false)) continue;
            if (!matchesExactOrContains(StringUtil.nvl(phone), c.getPhone(), true)) continue;
            out.add(c);
        }
        return out;
    }

    private boolean matchesExactOrContains(String crit, String value, boolean exactIfDigits) {
        if (StringUtil.isBlank(crit)) return true;
        String v = StringUtil.nvl(value);
        if (exactIfDigits && crit.matches("\\d+")) {
            return v.equalsIgnoreCase(crit);
        }
        return v.toLowerCase().contains(crit.toLowerCase());
    }
}
