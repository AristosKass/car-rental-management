package gui.panel;

import api.AppContext;
import api.exception.ValidationException;
import api.model.Car;
import api.model.Customer;
import api.model.Rental;
import gui.Ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>Panel Επιστροφής Οχήματος</h2>
 * Προβάλλει τις <b>ενεργές</b> ενοικιάσεις και επιτρέπει την ολοκλήρωση (return).
 * <p>
 * Παρέχει αναζήτηση ενεργής ενοικίασης με:
 * <ul>
 *   <li>πινακίδα οχήματος ή</li>
 *   <li>ΑΦΜ πελάτη</li>
 * </ul>
 * και στη συνέχεια, με επιλογή γραμμής, εκτελείται {@link api.service.RentalService#returnRental(String, LocalDate)}.
 */
public class ReturnPanel extends JPanel {

    private final AppContext ctx;

    // Search fields
    private final JTextField plateField = new JTextField(10);
    private final JTextField afmField = new JTextField(10);

    // Results table
    private final DefaultTableModel model = TableUtil.nonEditableModel(
            new String[]{"Rental ID", "Πινακίδα", "ΑΦΜ", "Πελάτης", "Έναρξη", "Λήξη", "Υπάλληλος"}
    );
    private final JTable table = new JTable(model);

    /**
     * Mapping γραμμής-πίνακα -> rentalId. Χρήσιμο γιατί ο πίνακας μπορεί να φιλτραριστεί/ανανεωθεί.
     */
    private final List<String> rentalIds = new ArrayList<>();

    /**
     * @param ctx application context
     */
    public ReturnPanel(AppContext ctx) {
        super(new BorderLayout(10, 10));
        this.ctx = ctx;

        add(buildSearchBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refreshTable(ctx.rentals().activeRentals());
    }

    /**
     * @return search bar (πινακίδα/ΑΦΜ) + κουμπιά
     */
    private JComponent buildSearchBar() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Αναζήτηση Ενεργής Ενοικίασης"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 0;
        p.add(new JLabel("Πινακίδα:"), gc);
        gc.gridx = 1;
        p.add(plateField, gc);

        gc.gridx = 2;
        p.add(new JLabel("ΑΦΜ:"), gc);
        gc.gridx = 3;
        p.add(afmField, gc);

        JButton searchBtn = new JButton("Αναζήτηση");
        searchBtn.addActionListener(e -> doSearch());

        JButton allBtn = new JButton("Όλες οι ενεργές");
        allBtn.addActionListener(e -> refreshTable(ctx.rentals().activeRentals()));

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 4;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(allBtn);
        actions.add(searchBtn);
        p.add(actions, gc);

        return p;
    }

    /**
     * @return κάτω action bar με το κουμπί επιστροφής
     */
    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton returnBtn = new JButton("Επιστροφή Επιλεγμένης");
        returnBtn.addActionListener(e -> doReturn());
        p.add(returnBtn);
        return p;
    }

    /**
     * Εκτελεί αναζήτηση ενεργών ενοικιάσεων με βάση πινακίδα ή ΑΦΜ.
     */
    private void doSearch() {
        String plate = plateField.getText().trim();
        String afm = afmField.getText().trim();

        List<Rental> results;
        if (!plate.isEmpty()) {
            Car car = ctx.cars().findByPlate(plate);
            if (car == null) {
                Ui.showError(this, "Δεν βρέθηκε όχημα με αυτή την πινακίδα.");
                return;
            }
            results = ctx.rentals().activeRentalsByCar(car.getId());
        } else if (!afm.isEmpty()) {
            results = ctx.rentals().activeRentalsByCustomer(afm);
        } else {
            Ui.showError(this, "Συμπληρώστε πινακίδα ή ΑΦΜ.");
            return;
        }

        refreshTable(results);
    }

    /**
     * Ολοκληρώνει την επιστροφή για την επιλεγμένη ενοικίαση.
     */
    private void doReturn() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Ui.showError(this, "Επιλέξτε ενοικίαση από τον πίνακα.");
            return;
        }
        String rentalId = rentalIds.get(row);

        if (!Ui.confirm(this, "Να ολοκληρωθεί η επιστροφή για Rental ID: " + rentalId + " ;")) return;

        try {
            ctx.rentals().returnRental(rentalId, LocalDate.now());
            Ui.showInfo(this, "Η επιστροφή ολοκληρώθηκε.");
            refreshTable(ctx.rentals().activeRentals());
        } catch (ValidationException ex) {
            Ui.showError(this, ex.getMessage());
        } catch (Exception ex) {
            Ui.showError(this, "Απρόσμενο σφάλμα: " + ex.getMessage());
        }
    }

    /**
     * Ανανεώνει τον πίνακα με τις δοθείσες ενοικιάσεις.
     *
     * @param rentals λίστα ενοικιάσεων (συνήθως ενεργές)
     */
    private void refreshTable(List<Rental> rentals) {
        model.setRowCount(0);
        rentalIds.clear();

        for (Rental r : rentals) {
            Car car = ctx.cars().findById(r.getCarId());
            Customer cust = ctx.customers().findByAfm(r.getCustomerAfm());

            model.addRow(new Object[]{
                    r.getRentalId(),
                    car == null ? "" : car.getPlate(),
                    r.getCustomerAfm(),
                    cust == null ? "" : cust.getFullName(),
                    r.getStartDate(),
                    r.getEndDate(),
                    r.getEmployeeUsername()
            });
            rentalIds.add(r.getRentalId());
        }
    }
}
