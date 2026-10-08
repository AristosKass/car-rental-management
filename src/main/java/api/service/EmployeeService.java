package api.service;

import api.model.Employee;
import api.repo.EmployeeRepository;

import java.util.List;
import java.util.Objects;

/**
 * <h2>EmployeeService</h2>
 * Επιχειρησιακές λειτουργίες για χρήστες/υπαλλήλους (employees).
 * <p>
 * Η εφαρμογή χρησιμοποιεί employees για login (username/password) και για να «χρεώνει»
 * κάθε ενοικίαση στον συνδεδεμένο υπάλληλο.
 * <p>
 * <b>Σημείωση αρχιτεκτονικής:</b> Το service αυτό αποτελεί «λεπτό» layer πάνω από το
 * {@link EmployeeRepository}. Τα βασικά validations (μοναδικό username/email, υποχρεωτικά πεδία)
 * υλοποιούνται στο repository, ώστε το GUI να παραμένει απλό.
 */
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * @param employeeRepository repo χρηστών
     */
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Επιστρέφει όλους τους χρήστες.
     *
     * @return λίστα employees (unmodifiable όπως επιστρέφεται από το repository)
     */
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    /**
     * Αναζητά χρήστη με βάση το username.
     *
     * @param username username (case-insensitive)
     * @return ο χρήστης ή {@code null} αν δεν βρεθεί
     */
    public Employee findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    /**
     * Προσθέτει νέο χρήστη.
     * <p>
     * Το {@link EmployeeRepository} θα:
     * <ul>
     *   <li>ελέγξει υποχρεωτικά πεδία,</li>
     *   <li>ελέγξει μοναδικότητα username/email,</li>
     *   <li>και θα αποθηκεύσει σε {@code data/users.csv}.</li>
     * </ul>
     *
     * @param employee ο νέος employee
     * @throws NullPointerException αν {@code employee} είναι {@code null}
     */
    public void add(Employee employee) {
        Objects.requireNonNull(employee, "employee");
        employeeRepository.add(employee);
    }

    /**
     * Διαγράφει χρήστη με βάση το username.
     * <p>
     * Η αποθήκευση γίνεται αυτόματα από το {@link EmployeeRepository}.
     *
     * @param username username προς διαγραφή
     */
    public void deleteByUsername(String username) {
        employeeRepository.deleteByUsername(username);
    }
}
