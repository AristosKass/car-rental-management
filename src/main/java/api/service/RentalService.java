package api.service;

import api.exception.NotFoundException;
import api.exception.ValidationException;
import api.model.Car;
import api.model.CarStatus;
import api.model.Rental;
import api.repo.CarRepository;
import api.repo.CustomerRepository;
import api.repo.RentalRepository;
import api.util.StringUtil;
import api.util.Validators;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <h2>RentalService</h2>
 * Επιχειρησιακή λογική για ενοικιάσεις.
 * <p>
 * Η κλάση είναι υπεύθυνη για:
 * <ul>
 *   <li>Καταχώρηση νέας ενοικίασης (έλεγχος διαθεσιμότητας οχήματος).</li>
 *   <li>Ολοκλήρωση/επιστροφή ενοικίασης.</li>
 *   <li>Εύρεση ιστορικού ενοικιάσεων ανά πελάτη ή ανά όχημα.</li>
 * </ul>
 * <p>
 * Σημαντικό: Κατά τη δημιουργία/επιστροφή ενοικίασης ενημερώνεται και η κατάσταση του οχήματος
 * ({@link CarStatus#AVAILABLE} / {@link CarStatus#RENTED}) και γίνεται αποθήκευση σε αρχεία.
 * <p>
 * Το service δεν έχει καμία εξάρτηση από GUI (Swing). Επιστρέφει domain objects ή πετάει
 * domain exceptions ({@link ValidationException}, {@link NotFoundException}).
 */
public class RentalService {

    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final RentalRepository rentalRepository;

    /**
     * Κατασκευαστής.
     *
     * @param carRepository      repository οχημάτων
     * @param customerRepository repository πελατών
     * @param rentalRepository   repository ενοικιάσεων
     */
    public RentalService(CarRepository carRepository,
                         CustomerRepository customerRepository,
                         RentalRepository rentalRepository) {
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.rentalRepository = rentalRepository;
    }

    /**
     * Δημιουργεί νέα ενοικίαση.
     * <p>
     * Κανόνες (validation):
     * <ul>
     *   <li>Το {@code carId} πρέπει να είναι θετικό και το όχημα να υπάρχει.</li>
     *   <li>Το όχημα πρέπει να είναι {@link CarStatus#AVAILABLE}.</li>
     *   <li>Το {@code customerAfm} πρέπει να είναι έγκυρο (9 ψηφία) και ο πελάτης να υπάρχει.</li>
     *   <li>Οι ημερομηνίες start/end είναι υποχρεωτικές και {@code endDate >= startDate}.</li>
     *   <li>Το {@code employeeUsername} είναι υποχρεωτικό (ποιος υπάλληλος κατέγραψε την ενοικίαση).</li>
     * </ul>
     * <p>
     * Side effects:
     * <ul>
     *   <li>Προστίθεται εγγραφή ενοικίασης στο {@code data/rentals.csv}.</li>
     *   <li>Το όχημα αλλάζει κατάσταση σε {@link CarStatus#RENTED} και αποθηκεύεται στο {@code data/vehicles.csv}.</li>
     * </ul>
     *
     * @param carId            id οχήματος
     * @param customerAfm      ΑΦΜ πελάτη
     * @param startDate        ημερομηνία έναρξης
     * @param endDate          ημερομηνία λήξης
     * @param employeeUsername username υπαλλήλου που καταχώρησε την ενοικίαση
     * @return το νέο {@link Rental}
     * @throws ValidationException αν παραβιαστούν οι κανόνες εισόδου
     * @throws NotFoundException   αν δεν βρεθεί όχημα/πελάτης
     */
    public Rental createRental(int carId,
                               String customerAfm,
                               LocalDate startDate,
                               LocalDate endDate,
                               String employeeUsername) {

        Validators.requirePositive(carId, "Επιλέξτε όχημα.");
        Validators.requireAfm(customerAfm);
        if (startDate == null) throw new ValidationException("Η ημερομηνία έναρξης είναι υποχρεωτική.");
        if (endDate == null) throw new ValidationException("Η ημερομηνία λήξης είναι υποχρεωτική.");
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("Η ημερομηνία λήξης δεν μπορεί να είναι πριν από την έναρξη.");
        }
        Validators.requireNotBlank(employeeUsername, "Απαιτείται συνδεδεμένος υπάλληλος.");

        Car car = carRepository.findById(carId);
        if (car == null) throw new NotFoundException("Το όχημα δεν βρέθηκε.");
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new ValidationException("Το όχημα δεν είναι διαθέσιμο.");
        }

        if (customerRepository.findByAfm(customerAfm) == null) {
            throw new NotFoundException("Ο πελάτης δεν βρέθηκε. Καταχωρήστε πρώτα τον πελάτη.");
        }

        String rentalId = UUID.randomUUID().toString();
        Rental rental = new Rental(
                rentalId,
                carId,
                customerAfm,
                startDate,
                endDate,
                false,
                null,
                StringUtil.nvl(employeeUsername).trim()
        );

        // 1) Αποθήκευση ενοικίασης
        rentalRepository.add(rental);

        // 2) Ενημέρωση κατάστασης οχήματος + αποθήκευση οχημάτων
        car.setStatus(CarStatus.RENTED);
        carRepository.update(car);
        saveCarsOrThrow();

        return rental;
    }

    /**
     * Ολοκληρώνει μία ενοικίαση (επιστροφή οχήματος).
     * <p>
     * Κανόνες:
     * <ul>
     *   <li>Το {@code rentalId} πρέπει να αντιστοιχεί σε υπαρκτή ενοικίαση.</li>
     *   <li>Η ενοικίαση δεν πρέπει να είναι ήδη ολοκληρωμένη.</li>
     * </ul>
     * <p>
     * Side effects:
     * <ul>
     *   <li>Η ενοικίαση σημειώνεται ως returned και αποθηκεύεται στο {@code data/rentals.csv}.</li>
     *   <li>Το όχημα αλλάζει κατάσταση σε {@link CarStatus#AVAILABLE} και αποθηκεύεται στο {@code data/vehicles.csv}.</li>
     * </ul>
     *
     * @param rentalId   κωδικός ενοικίασης
     * @param returnDate ημερομηνία επιστροφής (αν {@code null} χρησιμοποιείται η σημερινή)
     * @throws NotFoundException   αν δεν βρεθεί ενοικίαση/όχημα
     * @throws ValidationException αν η ενοικίαση είναι ήδη ολοκληρωμένη
     */
    public void returnRental(String rentalId, LocalDate returnDate) {
        if (StringUtil.isBlank(rentalId)) {
            throw new ValidationException("Ο κωδικός ενοικίασης είναι υποχρεωτικός.");
        }
        LocalDate rd = (returnDate == null) ? LocalDate.now() : returnDate;

        Rental r = rentalRepository.findById(rentalId);
        if (r == null) throw new NotFoundException("Η ενοικίαση δεν βρέθηκε.");
        if (r.isReturned()) throw new ValidationException("Η ενοικίαση έχει ήδη ολοκληρωθεί.");

        Car car = carRepository.findById(r.getCarId());
        if (car == null) throw new NotFoundException("Το όχημα της ενοικίασης δεν βρέθηκε.");

        r.markReturned(rd);
        rentalRepository.update(r);

        car.setStatus(CarStatus.AVAILABLE);
        carRepository.update(car);
        saveCarsOrThrow();
    }

    /**
     * @return όλες τις ενοικιάσεις (ενεργές + ολοκληρωμένες)
     */
    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    /**
     * @return μόνο τις ενεργές ενοικιάσεις (returned == false)
     */
    public List<Rental> activeRentals() {
        return rentalRepository.findActiveRentals();
    }

    /**
     * Ιστορικό ενοικιάσεων για συγκεκριμένο πελάτη.
     *
     * @param afm ΑΦΜ πελάτη
     * @return λίστα ενοικιάσεων του πελάτη
     */
    public List<Rental> historyByCustomer(String afm) {
        return rentalRepository.findByCustomerAfm(afm);
    }

    /**
     * Ιστορικό ενοικιάσεων για συγκεκριμένο όχημα.
     *
     * @param carId id οχήματος
     * @return λίστα ενοικιάσεων του οχήματος
     */
    public List<Rental> historyByCar(int carId) {
        return rentalRepository.findByCarId(carId);
    }

    /**
     * @param afm ΑΦΜ πελάτη
     * @return ενεργές ενοικιάσεις του πελάτη (filter πάνω στο ιστορικό)
     */
    public List<Rental> activeRentalsByCustomer(String afm) {
        List<Rental> out = new ArrayList<>();
        for (Rental r : rentalRepository.findByCustomerAfm(afm)) {
            if (!r.isReturned()) out.add(r);
        }
        return out;
    }

    /**
     * @param carId id οχήματος
     * @return ενεργές ενοικιάσεις του οχήματος (filter πάνω στο ιστορικό)
     */
    public List<Rental> activeRentalsByCar(int carId) {
        List<Rental> out = new ArrayList<>();
        for (Rental r : rentalRepository.findByCarId(carId)) {
            if (!r.isReturned()) out.add(r);
        }
        return out;
    }

    /**
     * Αποθηκεύει το CSV οχημάτων και τυλίγει το {@link IOException} σε {@link RuntimeException}.
     * <p>
     * Στόχος: το GUI να μη χρειάζεται να χειριστεί checked exceptions.
     */
    private void saveCarsOrThrow() {
        try {
            carRepository.save();
        } catch (IOException e) {
            throw new RuntimeException("Σφάλμα αποθήκευσης οχημάτων: " + e.getMessage(), e);
        }
    }
}
