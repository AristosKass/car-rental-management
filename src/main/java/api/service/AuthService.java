package api.service;

import api.exception.AuthException;
import api.model.Employee;
import api.repo.EmployeeRepository;
import api.util.StringUtil;
import api.util.Validators;

/**
 * <h2>AuthService</h2>
 * Υπηρεσία αυθεντικοποίησης (login/logout) και διαχείρισης «session» χρήστη.
 * <p>
 * Κρατάει στη μνήμη τον τρέχοντα συνδεδεμένο {@link Employee} (δεν υπάρχει persistence session
 * γιατί πρόκειται για desktop εφαρμογή).
 */
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private Employee current;

    /**
     * @param employeeRepository repo χρηστών
     */
    public AuthService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Προσπαθεί να κάνει login.
     *
     * @param username username
     * @param password password
     * @return τον συνδεδεμένο employee
     * @throws AuthException αν αποτύχει username/password
     */
    public Employee login(String username, String password) {
        Validators.requireNotBlank(username, "Το username είναι υποχρεωτικό.");
        Validators.requireNotBlank(password, "Το password είναι υποχρεωτικό.");

        Employee e = employeeRepository.findByUsername(username);
        if (e == null || !StringUtil.nvl(e.getPassword()).equals(password)) {
            throw new AuthException("Λάθος username ή password.");
        }
        current = e;
        return e;
    }

    /**
     * Κάνει logout (καθαρίζει το current session).
     */
    public void logout() {
        current = null;
    }

    /**
     * @return τρέχων συνδεδεμένος employee ή {@code null}
     */
    public Employee getCurrent() {
        return current;
    }

    /**
     * Βολική μέθοδος για κλήσεις που απαιτούν username (π.χ. καταχώρηση ενοικίασης).
     *
     * @return username τρέχοντος χρήστη
     * @throws AuthException αν δεν υπάρχει συνδεδεμένος χρήστης
     */
    public String getCurrentUsernameRequired() {
        if (current == null) throw new AuthException("Δεν υπάρχει συνδεδεμένος χρήστης.");
        return current.getUsername();
    }
}
