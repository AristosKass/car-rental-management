package api.repo;

import api.model.Rental;
import api.util.AppFiles;
import api.util.CsvUtil;
import api.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * <h2>RentalRepository</h2>
 * Repository ενοικιάσεων με αποθήκευση σε CSV.
 * <p>
 * <b>CSV file:</b> {@code data/rentals.csv} (βλ. {@link AppFiles#rentalsFile()}).
 * <p>
 * <b>CSV header:</b>
 * <pre>
 * rentalId,carId,customerAfm,startDate,endDate,returned,returnDate,employeeUsername
 * </pre>
 * όπου οι ημερομηνίες είναι ISO-8601 (yyyy-MM-dd).
 */
public class RentalRepository {

    private final File file;
    private final List<Rental> rentals = new ArrayList<>();

    /**
     * Δημιουργεί repository και φορτώνει από το CSV.
     */
    public RentalRepository() {
        this.file = AppFiles.rentalsFile();
        reload();
    }

    /**
     * Επαναφορτώνει από το CSV.
     */
    public final void reload() {
        rentals.clear();
        try {
            List<List<String>> rows = CsvUtil.readAll(file);
            if (rows.isEmpty()) return;

            // Header expected at row[0]
            for (int i = 1; i < rows.size(); i++) {
                List<String> r = rows.get(i);
                if (r.size() < 8) continue;

                String rentalId = r.get(0);
                int carId = Integer.parseInt(r.get(1));
                String customerAfm = r.get(2);
                LocalDate startDate = DateUtil.parse(r.get(3));
                LocalDate endDate = DateUtil.parse(r.get(4));
                boolean returned = Boolean.parseBoolean(r.get(5));
                LocalDate returnDate = DateUtil.parseNullable(r.get(6));
                String employeeUsername = r.get(7);

                rentals.add(new Rental(rentalId, carId, customerAfm, startDate, endDate, returned, returnDate, employeeUsername));
            }
        } catch (IOException e) {
            throw new RuntimeException("Σφάλμα ανάγνωσης αρχείου ενοικιάσεων: " + e.getMessage(), e);
        }
    }

    /**
     * @return unmodifiable λίστα όλων των ενοικιάσεων
     */
    public List<Rental> findAll() {
        return Collections.unmodifiableList(rentals);
    }

    /**
     * @param rentalId κωδικός ενοικίασης
     * @return ενοικίαση ή {@code null} αν δεν βρέθηκε
     */
    public Rental findById(String rentalId) {
        if (rentalId == null) return null;
        for (Rental r : rentals) {
            if (rentalId.equalsIgnoreCase(r.getRentalId())) return r;
        }
        return null;
    }

    /**
     * @param carId id οχήματος
     * @return όλες οι ενοικιάσεις του οχήματος
     */
    public List<Rental> findByCarId(int carId) {
        List<Rental> out = new ArrayList<>();
        for (Rental r : rentals) {
            if (r.getCarId() == carId) out.add(r);
        }
        return out;
    }

    /**
     * @param afm ΑΦΜ πελάτη
     * @return όλες οι ενοικιάσεις του πελάτη
     */
    public List<Rental> findByCustomerAfm(String afm) {
        // NOTE: Δεν χρησιμοποιούμε List.of() ώστε ο κώδικας να είναι συμβατός και με Java 8.
        if (afm == null) return Collections.emptyList();
        List<Rental> out = new ArrayList<>();
        for (Rental r : rentals) {
            if (afm.equalsIgnoreCase(r.getCustomerAfm())) out.add(r);
        }
        return out;
    }

    /**
     * Προσθέτει νέα ενοικίαση.
     *
     * @param rental ενοικίαση
     */
    public void add(Rental rental) {
        rentals.add(rental);
        save();
    }

    /**
     * Επιστρέφει τις <b>ενεργές</b> ενοικιάσεις (δηλαδή όσες δεν έχουν επιστραφεί).
     * <p>
     * Χρήσιμο για την οθόνη «Επιστροφή Οχήματος», όπου πρέπει να εμφανίζονται μόνο
     * οι ενοικιάσεις που είναι ακόμη σε εξέλιξη.
     *
     * @return λίστα ενεργών ενοικιάσεων (νέα λίστα)
     */
    public List<Rental> findActiveRentals() {
        List<Rental> out = new ArrayList<>();
        for (Rental r : rentals) {
            if (!r.isReturned()) out.add(r);
        }
        return out;
    }

    /**
     * Ενημερώνει υπάρχον record (με βάση το rentalId).
     * Αν δεν βρεθεί, το προσθέτει (defensive).
     *
     * @param rental ενοικίαση
     */
    public void update(Rental rental) {
        for (int i = 0; i < rentals.size(); i++) {
            if (rentals.get(i).getRentalId().equalsIgnoreCase(rental.getRentalId())) {
                rentals.set(i, rental);
                save();
                return;
            }
        }
        rentals.add(rental);
        save();
    }

    /**
     * Γράφει ολόκληρη τη λίστα στο CSV.
     */
    private void save() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                "rentalId", "carId", "customerAfm", "startDate", "endDate", "returned", "returnDate", "employeeUsername"
        ));
        for (Rental r : rentals) {
            rows.add(Arrays.asList(
                    r.getRentalId(),
                    String.valueOf(r.getCarId()),
                    r.getCustomerAfm(),
                    DateUtil.format(r.getStartDate()),
                    DateUtil.format(r.getEndDate()),
                    String.valueOf(r.isReturned()),
                    DateUtil.formatNullable(r.getReturnDate()),
                    r.getEmployeeUsername()
            ));
        }
        try {
            CsvUtil.writeAll(file, rows);
        } catch (IOException ex) {
            throw new RuntimeException("Σφάλμα αποθήκευσης αρχείου ενοικιάσεων: " + ex.getMessage(), ex);
        }
    }
}
