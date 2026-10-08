package gui.panel;

import api.AppContext;
import api.model.Car;
import api.model.Customer;
import api.model.Rental;
import gui.Ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * <h2>Panel Ιστορικού Ενοικιάσεων</h2>
 * Προβάλλει ιστορικό ενοικιάσεων:
 * <ul>
 *   <li>ανά πελάτη (με ΑΦΜ)</li>
 *   <li>ανά όχημα (με πινακίδα)</li>
 * </ul>
 * <p>
 * Το ιστορικό περιλαμβάνει τόσο ενεργές ενοικιάσεις (returned=false) όσο και ολοκληρωμένες.
 */
public class HistoryPanel extends JPanel {

    /**
     * @param ctx application context
     */
    public HistoryPanel(AppContext ctx) {
        super(new BorderLayout(10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ανά Πελάτη", new CustomerHistoryPanel(ctx));
        tabs.addTab("Ανά Όχημα", new CarHistoryPanel(ctx));

        add(tabs, BorderLayout.CENTER);
    }

    /**
     * Υπο-panel που εμφανίζει ιστορικό με βάση το ΑΦΜ πελάτη.
     */
    private static class CustomerHistoryPanel extends JPanel {

        private final AppContext ctx;
        private final JTextField afmField = new JTextField(12);

        private final DefaultTableModel model = TableUtil.nonEditableModel(
                new String[]{"Rental ID", "Πινακίδα", "Μάρκα", "Μοντέλο", "Έναρξη", "Λήξη", "Returned", "Return Date", "Υπάλληλος"}
        );
        private final JTable table = new JTable(model);

        /**
         * @param ctx application context
         */
        public CustomerHistoryPanel(AppContext ctx) {
            super(new BorderLayout(8, 8));
            this.ctx = ctx;

            add(buildTop(), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
        }

        /**
         * @return μπάρα εισαγωγής ΑΦΜ + κουμπιά
         */
        private JComponent buildTop() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.setBorder(BorderFactory.createTitledBorder("Ιστορικό Ενοικιάσεων Πελάτη"));

            p.add(new JLabel("ΑΦΜ:"));
            p.add(afmField);

            JButton showBtn = new JButton("Προβολή");
            showBtn.addActionListener(e -> doShow());
            p.add(showBtn);

            JButton clearBtn = new JButton("Καθαρισμός");
            clearBtn.addActionListener(e -> model.setRowCount(0));
            p.add(clearBtn);

            return p;
        }

        /**
         * Φέρνει από το API το ιστορικό του πελάτη και γεμίζει τον πίνακα.
         */
        private void doShow() {
            String afm = afmField.getText().trim();
            if (afm.isEmpty()) {
                Ui.showError(this, "Συμπληρώστε ΑΦΜ.");
                return;
            }
            Customer cust = ctx.customers().findByAfm(afm);
            if (cust == null) {
                Ui.showError(this, "Ο πελάτης δεν βρέθηκε.");
                return;
            }

            List<Rental> rentals = ctx.rentals().historyByCustomer(afm);
            model.setRowCount(0);
            for (Rental r : rentals) {
                Car car = ctx.cars().findById(r.getCarId());
                model.addRow(new Object[]{
                        r.getRentalId(),
                        car == null ? "" : car.getPlate(),
                        car == null ? "" : car.getBrand(),
                        car == null ? "" : car.getModel(),
                        r.getStartDate(),
                        r.getEndDate(),
                        r.isReturned(),
                        r.getReturnDate(),
                        r.getEmployeeUsername()
                });
            }
        }
    }

    /**
     * Υπο-panel που εμφανίζει ιστορικό με βάση την πινακίδα οχήματος.
     */
    private static class CarHistoryPanel extends JPanel {

        private final AppContext ctx;
        private final JTextField plateField = new JTextField(12);

        private final DefaultTableModel model = TableUtil.nonEditableModel(
                new String[]{"Rental ID", "ΑΦΜ", "Πελάτης", "Έναρξη", "Λήξη", "Returned", "Return Date", "Υπάλληλος"}
        );
        private final JTable table = new JTable(model);

        /**
         * @param ctx application context
         */
        public CarHistoryPanel(AppContext ctx) {
            super(new BorderLayout(8, 8));
            this.ctx = ctx;

            add(buildTop(), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
        }

        /**
         * @return μπάρα εισαγωγής πινακίδας + κουμπιά
         */
        private JComponent buildTop() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.setBorder(BorderFactory.createTitledBorder("Ιστορικό Ενοικιάσεων Οχήματος"));

            p.add(new JLabel("Πινακίδα:"));
            p.add(plateField);

            JButton showBtn = new JButton("Προβολή");
            showBtn.addActionListener(e -> doShow());
            p.add(showBtn);

            JButton clearBtn = new JButton("Καθαρισμός");
            clearBtn.addActionListener(e -> model.setRowCount(0));
            p.add(clearBtn);

            return p;
        }

        /**
         * Φέρνει από το API το ιστορικό για το συγκεκριμένο όχημα και γεμίζει τον πίνακα.
         */
        private void doShow() {
            String plate = plateField.getText().trim();
            if (plate.isEmpty()) {
                Ui.showError(this, "Συμπληρώστε πινακίδα.");
                return;
            }
            Car car = ctx.cars().findByPlate(plate);
            if (car == null) {
                Ui.showError(this, "Το όχημα δεν βρέθηκε.");
                return;
            }

            List<Rental> rentals = ctx.rentals().historyByCar(car.getId());
            model.setRowCount(0);
            for (Rental r : rentals) {
                Customer cust = ctx.customers().findByAfm(r.getCustomerAfm());
                model.addRow(new Object[]{
                        r.getRentalId(),
                        r.getCustomerAfm(),
                        cust == null ? "" : cust.getFullName(),
                        r.getStartDate(),
                        r.getEndDate(),
                        r.isReturned(),
                        r.getReturnDate(),
                        r.getEmployeeUsername()
                });
            }
        }
    }
}
