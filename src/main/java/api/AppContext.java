package api;

import api.repo.CarRepository;
import api.repo.CustomerRepository;
import api.repo.EmployeeRepository;
import api.repo.RentalRepository;
import api.service.AuthService;
import api.service.CarService;
import api.service.CustomerService;
import api.service.EmployeeService;
import api.service.RentalService;
import api.util.SeedInitializer;

import java.io.IOException;

/**
 * <h2>AppContext</h2>
 * Κεντρικό «container» εξαρτήσεων (mini dependency injection) της εφαρμογής.
 * <p>
 * Ο ρόλος του είναι να συγκεντρώνει σε ένα σημείο:
 * <ul>
 *   <li>Τα repositories (persistence layer / CSV αρχεία).</li>
 *   <li>Τα services (business rules / validation / συντονισμός repos).</li>
 * </ul>
 * ώστε το GUI να κάνει απλή πρόσβαση σε λειτουργίες χωρίς να «χτίζει» μόνο του
 * αλυσίδες dependencies.
 * <p>
 * <b>Αρχικοποίηση δεδομένων:</b> Στον constructor εκτελείται
 * {@link SeedInitializer#ensureInitialized()} ώστε, στην πρώτη εκτέλεση, να δημιουργηθεί
 * ο φάκελος {@code data/} και τα seed CSV αρχεία (users/vehicles) να αντιγραφούν/δημιουργηθούν.
 * Από τις επόμενες εκτελέσεις και μετά, η αρχικοποίηση δεν ξανατρέχει (δεν γίνεται reset).
 */
public class AppContext {

    // ---- Repositories ----
    /** Repository υπαλλήλων/χρηστών (CSV: data/users.csv). */
    private final EmployeeRepository employeeRepository;

    /** Repository οχημάτων (CSV: data/vehicles.csv). */
    private final CarRepository carRepository;

    /** Repository πελατών (CSV: data/customers.csv). */
    private final CustomerRepository customerRepository;

    /** Repository ενοικιάσεων (CSV: data/rentals.csv). */
    private final RentalRepository rentalRepository;

    // ---- Services ----
    private final AuthService authService;
    private final CarService carService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final RentalService rentalService;

    /**
     * Δημιουργεί το context.
     * <p>
     * Βήματα:
     * <ol>
     *   <li>Εξασφαλίζει ότι τα seed/persistence αρχεία υπάρχουν ({@link SeedInitializer}).</li>
     *   <li>Φορτώνει repositories από τα CSV.</li>
     *   <li>Δημιουργεί services που χρησιμοποιούν τα repositories.</li>
     * </ol>
     */
    public AppContext() {
        try {
            SeedInitializer.ensureInitialized();
        } catch (IOException e) {
            throw new RuntimeException("Αποτυχία αρχικοποίησης δεδομένων: " + e.getMessage(), e);
        }

        // Repos
        employeeRepository = new EmployeeRepository();
        carRepository = new CarRepository();
        customerRepository = new CustomerRepository();
        rentalRepository = new RentalRepository();

        // Services
        authService = new AuthService(employeeRepository);
        carService = new CarService(carRepository);
        customerService = new CustomerService(customerRepository);
        employeeService = new EmployeeService(employeeRepository);
        rentalService = new RentalService(carRepository, customerRepository, rentalRepository);
    }

    /**
     * @return service αυθεντικοποίησης (login/logout) και current session
     */
    public AuthService auth() {
        return authService;
    }

    /**
     * @return service διαχείρισης οχημάτων
     */
    public CarService cars() {
        return carService;
    }

    /**
     * @return service διαχείρισης πελατών
     */
    public CustomerService customers() {
        return customerService;
    }

    /**
     * @return service διαχείρισης υπαλλήλων/χρηστών
     */
    public EmployeeService employees() {
        return employeeService;
    }

    /**
     * @return service διαχείρισης ενοικιάσεων
     */
    public RentalService rentals() {
        return rentalService;
    }
}
