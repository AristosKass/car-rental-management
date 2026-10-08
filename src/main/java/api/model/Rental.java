package api.model;

import java.time.LocalDate;

/**
 * <h2>Rental (Ενοικίαση)</h2>
 * Εγγραφή ενοικίασης οχήματος.
 * <p>
 * Περιλαμβάνει:
 * <ul>
 *   <li>το όχημα ({@link #carId})</li>
 *   <li>τον πελάτη ({@link #customerAfm})</li>
 *   <li>ημερομηνίες έναρξης/λήξης</li>
 *   <li>κατάσταση επιστροφής (returned + returnDate)</li>
 *   <li>τον υπάλληλο που καταχώρησε την ενοικίαση</li>
 * </ul>
 */
public class Rental {

    /** Μοναδικός κωδικός ενοικίασης (UUID σε μορφή string). */
    private String rentalId;

    /** Αναφορά στο id του οχήματος. */
    private int carId;

    /** Αναφορά στο ΑΦΜ του πελάτη. */
    private String customerAfm;

    /** Ημερομηνία έναρξης. */
    private LocalDate startDate;

    /** Ημερομηνία λήξης. */
    private LocalDate endDate;

    /** True αν έχει ολοκληρωθεί η επιστροφή. */
    private boolean returned;

    /** Ημερομηνία πραγματικής επιστροφής (nullable). */
    private LocalDate returnDate;

    /** Username υπαλλήλου που καταχώρησε την ενοικίαση. */
    private String employeeUsername;

    /**
     * Δημιουργεί μια εγγραφή ενοικίασης.
     *
     * @param rentalId          μοναδικός κωδικός ενοικίασης
     * @param carId             id οχήματος
     * @param customerAfm       ΑΦΜ πελάτη
     * @param startDate         ημερομηνία έναρξης
     * @param endDate           ημερομηνία λήξης
     * @param returned          αν έχει επιστραφεί
     * @param returnDate        ημερομηνία επιστροφής (nullable)
     * @param employeeUsername  username υπαλλήλου
     */
    public Rental(String rentalId, int carId, String customerAfm,
                  LocalDate startDate, LocalDate endDate,
                  boolean returned, LocalDate returnDate,
                  String employeeUsername) {
        this.rentalId = rentalId;
        this.carId = carId;
        this.customerAfm = customerAfm;
        this.startDate = startDate;
        this.endDate = endDate;
        this.returned = returned;
        this.returnDate = returnDate;
        this.employeeUsername = employeeUsername;
    }

    /**
     * @return μοναδικός κωδικός ενοικίασης
     */
    public String getRentalId() {
        return rentalId;
    }

    /**
     * @return id οχήματος
     */
    public int getCarId() {
        return carId;
    }

    /**
     * @return ΑΦΜ πελάτη
     */
    public String getCustomerAfm() {
        return customerAfm;
    }

    /**
     * @return ημερομηνία έναρξης
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @return ημερομηνία λήξης
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @return {@code true} αν η ενοικίαση έχει επιστραφεί
     */
    public boolean isReturned() {
        return returned;
    }

    /**
     * @return ημερομηνία επιστροφής ή {@code null} αν δεν έχει επιστραφεί
     */
    public LocalDate getReturnDate() {
        return returnDate;
    }

    /**
     * @return username υπαλλήλου που καταχώρησε την ενοικίαση
     */
    public String getEmployeeUsername() {
        return employeeUsername;
    }

    /**
     * Σηματοδοτεί την ενοικίαση ως «επιστραφείσα».
     *
     * @param date ημερομηνία πραγματικής επιστροφής
     */
    public void markReturned(LocalDate date) {
        this.returned = true;
        this.returnDate = date;
    }
}
