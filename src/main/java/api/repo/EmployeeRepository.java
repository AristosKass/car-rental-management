package api.repo;

import api.exception.NotFoundException;
import api.exception.ValidationException;
import api.model.Employee;
import api.util.AppFiles;
import api.util.CsvUtil;
import api.util.StringUtil;
import api.util.Validators;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <h2>EmployeeRepository</h2>
 * Repository χρηστών/υπαλλήλων (employees) με αποθήκευση σε CSV.
 * <p>
 * <b>CSV file:</b> {@code data/users.csv} (βλ. {@link AppFiles#employeesFile()}).
 * <p>
 * <b>CSV header:</b>
 * <pre>
 * name,surname,username,email,password
 * </pre>
 * <p>
 * Ο κωδικός αποθηκεύεται ως απλό κείμενο (για την εργασία).
 */
public class EmployeeRepository {

    private final File file;
    private final List<Employee> employees = new ArrayList<>();

    /**
     * Δημιουργεί repository και φορτώνει δεδομένα από το CSV.
     */
    public EmployeeRepository() {
        this.file = AppFiles.employeesFile();
        reload();
    }

    /**
     * Επαναφορτώνει δεδομένα από δίσκο.
     *
     * @throws RuntimeException αν αποτύχει η ανάγνωση
     */
    public final void reload() {
        employees.clear();
        try {
            List<List<String>> rows = CsvUtil.readAll(file);
            if (rows.isEmpty()) return;

            // Expect header: name,surname,username,email,password
            for (int i = 1; i < rows.size(); i++) {
                List<String> r = rows.get(i);
                if (r.size() < 5) continue;
                String name = r.get(0);
                String surname = r.get(1);
                String username = r.get(2);
                String email = r.get(3);
                String password = r.get(4);
                employees.add(new Employee(name, surname, username, email, password));
            }
        } catch (IOException e) {
            throw new RuntimeException("Σφάλμα ανάγνωσης αρχείου χρηστών: " + e.getMessage(), e);
        }
    }

    /**
     * @return unmodifiable λίστα employees
     */
    public List<Employee> findAll() {
        return Collections.unmodifiableList(employees);
    }

    /**
     * Αναζητά employee με username.
     *
     * @param username username
     * @return employee ή {@code null}
     */
    public Employee findByUsername(String username) {
        for (Employee e : employees) {
            if (e.getUsername() != null && e.getUsername().equalsIgnoreCase(StringUtil.nvl(username))) return e;
        }
        return null;
    }

    /**
     * Προσθέτει νέο employee.
     *
     * @param employee employee προς προσθήκη
     * @throws ValidationException αν αποτύχει validation ή αν υπάρχει ήδη ίδιο username/email
     */
    public void add(Employee employee) {
        validateEmployee(employee, null);
        employees.add(employee);
        save();
    }

    /**
     * Διαγράφει employee με βάση το username.
     *
     * @param username username
     * @throws NotFoundException αν δεν βρέθηκε
     */
    public void deleteByUsername(String username) {
        Employee existing = findByUsername(username);
        if (existing == null) throw new NotFoundException("Ο χρήστης δεν βρέθηκε: " + username);
        employees.remove(existing);
        save();
    }

    /**
     * Validation employee στοιχείων (μοναδικότητα username/email).
     */
    private void validateEmployee(Employee e, Employee ignore) {
        if (e == null) throw new ValidationException("Κενός χρήστης.");
        Validators.requireNotBlank(e.getFullName(), "Το ονοματεπώνυμο είναι υποχρεωτικό.");
        Validators.requireNotBlank(e.getUsername(), "Το username είναι υποχρεωτικό.");
        Validators.requireNotBlank(e.getEmail(), "Το email είναι υποχρεωτικό.");
        Validators.requireNotBlank(e.getPassword(), "Το password είναι υποχρεωτικό.");

        for (Employee other : employees) {
            if (ignore != null && other == ignore) continue;
            if (other.getUsername().equalsIgnoreCase(e.getUsername())) {
                throw new ValidationException("Υπάρχει ήδη χρήστης με αυτό το username.");
            }
            if (StringUtil.nvl(other.getEmail()).equalsIgnoreCase(StringUtil.nvl(e.getEmail()))) {
                throw new ValidationException("Υπάρχει ήδη χρήστης με αυτό το email.");
            }
        }
    }

    /**
     * Γράφει ολόκληρη τη λίστα στο CSV.
     */
    private void save() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("name", "surname", "username", "email", "password"));
        for (Employee e : employees) {
            rows.add(Arrays.asList(
                    e.getName(),
                    e.getSurname(),
                    e.getUsername(),
                    e.getEmail(),
                    e.getPassword()
            ));
        }
        try {
            CsvUtil.writeAll(file, rows);
        } catch (IOException ex) {
            throw new RuntimeException("Σφάλμα αποθήκευσης αρχείου χρηστών: " + ex.getMessage(), ex);
        }
    }
}
