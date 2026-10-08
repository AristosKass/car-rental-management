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
import java.util.List;

/**
 * <h2>Panel Νέας Ενοικίασης</h2>
 * Υλοποιεί τη ροή δημιουργίας νέας ενοικίασης σε 3 βήματα:
 * <ol>
 *   <li>Επιλογή διαθέσιμου οχήματος (αναζήτηση + πίνακας).</li>
 *   <li>Επιλογή πελάτη (αναζήτηση + πίνακας).</li>
 *   <li>Συμπλήρωση ημερομηνιών και καταχώρηση της ενοικίασης.</li>
 * </ol>
 * <p>
 * Η πραγματική δημιουργία ενοικίασης γίνεται στο {@link api.service.RentalService}.
 */
public class NewRentalPanel extends JPanel {

    private final AppContext ctx;

    // ---- Car search ----
    private final JTextField carBrandField = new JTextField(10);
    private final JTextField carPlateField = new JTextField(10);
    private final DefaultTableModel carsModel = TableUtil.nonEditableModel(
            new String[]{"ID", "Πινακίδα", "Μάρκα", "Τύπος", "Μοντέλο", "Έτος", "Χρώμα"}
    );
    private final JTable carsTable = new JTable(carsModel);

    // ---- Customer search ----
    private final JTextField custAfmField = new JTextField(10);
    private final JTextField custNameField = new JTextField(10);
    private final DefaultTableModel custModel = TableUtil.nonEditableModel(
            new String[]{"ΑΦΜ", "Ονοματεπώνυμο", "Τηλέφωνο"}
    );
    private final JTable custTable = new JTable(custModel);

    // ---- Rental fields ----
    private final JTextField startDateField = new JTextField(10);
    private final JTextField endDateField = new JTextField(10);

    /**
     * @param ctx application context
     */
    public NewRentalPanel(AppContext ctx) {
        super(new BorderLayout(10, 10));
        this.ctx = ctx;

        // Default dates: σήμερα -> αύριο
        startDateField.setText(LocalDate.now().toString());
        endDateField.setText(LocalDate.now().plusDays(1).toString());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildCarsPanel(), buildCustomersPanel());
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);
        add(buildCreatePanel(), BorderLayout.SOUTH);

        // Αρχική φόρτωση δεδομένων
        refreshCars(ctx.cars().availableCars());
        refreshCustomers(ctx.customers().findAll());
    }

    /**
     * @return panel επιλογής διαθέσιμου οχήματος (αναζήτηση + πίνακας)
     */
    private JComponent buildCarsPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createTitledBorder("1) Επιλέξτε Διαθέσιμο Όχημα"));

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.add(new JLabel("Μάρκα:"));
        search.add(carBrandField);
        search.add(new JLabel("Πινακίδα:"));
        search.add(carPlateField);

        JButton searchBtn = new JButton("Αναζήτηση");
        searchBtn.addActionListener(e -> doCarSearch());

        JButton clearBtn = new JButton("Καθαρισμός");
        clearBtn.addActionListener(e -> {
            carBrandField.setText("");
            carPlateField.setText("");
            refreshCars(ctx.cars().availableCars());
        });

        search.add(clearBtn);
        search.add(searchBtn);

        p.add(search, BorderLayout.NORTH);
        p.add(new JScrollPane(carsTable), BorderLayout.CENTER);
        return p;
    }

    /**
     * @return panel επιλογής πελάτη (αναζήτηση + πίνακας)
     */
    private JComponent buildCustomersPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createTitledBorder("2) Επιλέξτε Πελάτη"));

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.add(new JLabel("ΑΦΜ:"));
        search.add(custAfmField);
        search.add(new JLabel("Όνομα:"));
        search.add(custNameField);

        JButton searchBtn = new JButton("Αναζήτηση");
        searchBtn.addActionListener(e -> doCustomerSearch());

        JButton clearBtn = new JButton("Καθαρισμός");
        clearBtn.addActionListener(e -> {
            custAfmField.setText("");
            custNameField.setText("");
            refreshCustomers(ctx.customers().findAll());
        });

        search.add(clearBtn);
        search.add(searchBtn);

        p.add(search, BorderLayout.NORTH);
        p.add(new JScrollPane(custTable), BorderLayout.CENTER);
        return p;
    }

    /**
     * @return κάτω panel με ημερομηνίες + κουμπί δημιουργίας ενοικίασης
     */
    private JComponent buildCreatePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("3) Δημιουργία Ενοικίασης"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 0;
        p.add(new JLabel("Ημ/νία έναρξης (yyyy-MM-dd):"), gc);
        gc.gridx = 1;
        p.add(startDateField, gc);

        gc.gridx = 2;
        p.add(new JLabel("Ημ/νία λήξης (yyyy-MM-dd):"), gc);
        gc.gridx = 3;
        p.add(endDateField, gc);

        JButton createBtn = new JButton("Καταχώρηση Ενοικίασης");
        createBtn.addActionListener(e -> doCreate());

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 4;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(createBtn);
        p.add(actions, gc);

        return p;
    }

    /**
     * Αναζήτηση οχήματος. Κρατά μόνο διαθέσιμα.
     */
    private void doCarSearch() {
        List<Car> res = ctx.cars().search(carBrandField.getText(), carPlateField.getText(), "", "", null);
        // κρατάμε μόνο διαθέσιμα (extra φίλτρο στο GUI)
        res.removeIf(c -> c.getStatus() != api.model.CarStatus.AVAILABLE);
        refreshCars(res);
    }

    /**
     * Αναζήτηση πελάτη με βάση ΑΦΜ ή μέρος ονόματος.
     */
    private void doCustomerSearch() {
        List<Customer> res = ctx.customers().search(custAfmField.getText(), custNameField.getText(), "");
        refreshCustomers(res);
    }

    /**
     * Δημιουργεί ενοικίαση για το επιλεγμένο όχημα/πελάτη.
     */
    private void doCreate() {
        Integer carId = selectedCarId();
        String afm = selectedCustomerAfm();
        if (carId == null) {
            Ui.showError(this, "Επιλέξτε διαθέσιμο όχημα.");
            return;
        }
        if (afm == null) {
            Ui.showError(this, "Επιλέξτε πελάτη.");
            return;
        }

        try {
            LocalDate start = LocalDate.parse(startDateField.getText().trim());
            LocalDate end = LocalDate.parse(endDateField.getText().trim());

            Rental r = ctx.rentals().createRental(
                    carId,
                    afm,
                    start,
                    end,
                    ctx.auth().getCurrentUsernameRequired()
            );

            Ui.showInfo(this, "Η ενοικίαση καταχωρήθηκε.\nΚωδικός ενοικίασης: " + r.getRentalId());
            refreshCars(ctx.cars().availableCars());
        } catch (ValidationException ex) {
            Ui.showError(this, ex.getMessage());
        } catch (Exception ex) {
            Ui.showError(this, "Απρόσμενο σφάλμα: " + ex.getMessage());
        }
    }

    /**
     * @return id επιλεγμένου οχήματος ή {@code null}
     */
    private Integer selectedCarId() {
        int row = carsTable.getSelectedRow();
        if (row < 0) return null;
        Object v = carsModel.getValueAt(row, 0);
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return ΑΦΜ επιλεγμένου πελάτη ή {@code null}
     */
    private String selectedCustomerAfm() {
        int row = custTable.getSelectedRow();
        if (row < 0) return null;
        Object v = custModel.getValueAt(row, 0);
        return v == null ? null : String.valueOf(v);
    }

    /**
     * Γεμίζει τον πίνακα οχημάτων.
     *
     * @param cars λίστα οχημάτων προς εμφάνιση
     */
    private void refreshCars(List<Car> cars) {
        carsModel.setRowCount(0);
        for (Car c : cars) {
            carsModel.addRow(new Object[]{c.getId(), c.getPlate(), c.getBrand(), c.getType(), c.getModel(), c.getYear(), c.getColor()});
        }
    }

    /**
     * Γεμίζει τον πίνακα πελατών.
     *
     * @param customers λίστα πελατών προς εμφάνιση
     */
    private void refreshCustomers(List<Customer> customers) {
        custModel.setRowCount(0);
        for (Customer c : customers) {
            custModel.addRow(new Object[]{c.getAfm(), c.getFullName(), c.getPhone()});
        }
    }
}
