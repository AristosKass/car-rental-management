package gui.panel;

import api.AppContext;
import api.model.Car;
import api.model.CarStatus;
import gui.Ui;
import gui.dialog.CarDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * <h2>Panel Διαχείρισης Οχημάτων</h2>
 * Παρέχει:
 * <ul>
 *   <li>Αναζήτηση οχημάτων με συνδυασμό κριτηρίων (μάρκα, πινακίδα, μοντέλο, χρώμα, κατάσταση).</li>
 *   <li>Προσθήκη νέου οχήματος.</li>
 *   <li>Επεξεργασία υπάρχοντος οχήματος.</li>
 * </ul>
 * <p>
 * Δεν υλοποιεί business logic: καλεί μόνο {@code ctx.cars()} (δηλ. {@link api.service.CarService}).
 */
public class CarsPanel extends JPanel {

    /** Application context (για πρόσβαση σε services). */
    private final AppContext ctx;

    // Search fields
    private final JTextField brandField = new JTextField(10);
    private final JTextField plateField = new JTextField(10);
    private final JTextField modelField = new JTextField(10);
    private final JTextField colorField = new JTextField(10);
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"(Όλα)", "Διαθέσιμο", "Ενοικιασμένο"});

    // Results table
    private final DefaultTableModel model = TableUtil.nonEditableModel(
            new String[]{"ID", "Πινακίδα", "Μάρκα", "Τύπος", "Μοντέλο", "Έτος", "Χρώμα", "Κατάσταση"}
    );
    private final JTable table = new JTable(model);

    /**
     * Δημιουργεί το panel και φορτώνει αρχικά όλα τα οχήματα.
     *
     * @param ctx application context
     */
    public CarsPanel(AppContext ctx) {
        super(new BorderLayout(10, 10));
        this.ctx = ctx;

        add(buildSearchBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refreshTable(ctx.cars().findAll());
    }

    /**
     * @return το επάνω τμήμα (κριτήρια + κουμπιά αναζήτησης/καθαρισμού)
     */
    private JComponent buildSearchBar() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Αναζήτηση Οχήματος"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;

        int x = 0;

        gc.gridx = x++;
        gc.gridy = 0;
        p.add(new JLabel("Μάρκα:"), gc);
        gc.gridx = x++;
        p.add(brandField, gc);

        gc.gridx = x++;
        p.add(new JLabel("Πινακίδα:"), gc);
        gc.gridx = x++;
        p.add(plateField, gc);

        gc.gridx = x++;
        p.add(new JLabel("Μοντέλο:"), gc);
        gc.gridx = x++;
        p.add(modelField, gc);

        gc.gridx = x++;
        p.add(new JLabel("Χρώμα:"), gc);
        gc.gridx = x++;
        p.add(colorField, gc);

        gc.gridx = x++;
        p.add(new JLabel("Κατάσταση:"), gc);
        gc.gridx = x++;
        p.add(statusBox, gc);

        JButton searchBtn = new JButton("Αναζήτηση");
        searchBtn.addActionListener(e -> doSearch());

        JButton clearBtn = new JButton("Καθαρισμός");
        clearBtn.addActionListener(e -> {
            brandField.setText("");
            plateField.setText("");
            modelField.setText("");
            colorField.setText("");
            statusBox.setSelectedIndex(0);
            refreshTable(ctx.cars().findAll());
        });

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = x;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(clearBtn);
        actions.add(searchBtn);
        p.add(actions, gc);

        return p;
    }

    /**
     * @return το κάτω τμήμα με κουμπιά «Προσθήκη»/«Επεξεργασία».
     */
    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addBtn = new JButton("Προσθήκη");
        addBtn.addActionListener(e -> onAdd());

        JButton editBtn = new JButton("Επεξεργασία");
        editBtn.addActionListener(e -> onEdit());

        p.add(addBtn);
        p.add(editBtn);
        return p;
    }

    /**
     * Εκτελεί αναζήτηση με βάση τα κριτήρια και ενημερώνει τον πίνακα.
     */
    private void doSearch() {
        CarStatus status = null;
        String sel = (String) statusBox.getSelectedItem();
        if ("Διαθέσιμο".equals(sel)) status = CarStatus.AVAILABLE;
        if ("Ενοικιασμένο".equals(sel)) status = CarStatus.RENTED;

        List<Car> results = ctx.cars().search(
                brandField.getText(),
                plateField.getText(),
                modelField.getText(),
                colorField.getText(),
                status
        );
        refreshTable(results);
    }

    /**
     * Ανοίγει το dialog προσθήκης οχήματος και ανανεώνει τον πίνακα αν αποθηκεύτηκε.
     */
    private void onAdd() {
        CarDialog dlg = new CarDialog(SwingUtilities.getWindowAncestor(this), ctx.cars(), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshTable(ctx.cars().findAll());
            Ui.showInfo(this, "Το όχημα προστέθηκε.");
        }
    }

    /**
     * Ανοίγει το dialog επεξεργασίας για το επιλεγμένο όχημα.
     */
    private void onEdit() {
        Integer carId = selectedCarId();
        if (carId == null) {
            Ui.showError(this, "Επιλέξτε όχημα από τον πίνακα.");
            return;
        }
        Car car = ctx.cars().findById(carId);
        if (car == null) {
            Ui.showError(this, "Το όχημα δεν βρέθηκε.");
            return;
        }

        CarDialog dlg = new CarDialog(SwingUtilities.getWindowAncestor(this), ctx.cars(), car);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshTable(ctx.cars().findAll());
            Ui.showInfo(this, "Το όχημα ενημερώθηκε.");
        }
    }

    /**
     * @return το id του επιλεγμένου οχήματος ή {@code null} αν δεν υπάρχει επιλογή.
     */
    private Integer selectedCarId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object v = model.getValueAt(row, 0);
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Γεμίζει τον πίνακα αποτελεσμάτων με τα δοθέντα οχήματα.
     *
     * @param cars λίστα οχημάτων προς εμφάνιση
     */
    private void refreshTable(List<Car> cars) {
        model.setRowCount(0);
        for (Car c : cars) {
            model.addRow(new Object[]{
                    c.getId(),
                    c.getPlate(),
                    c.getBrand(),
                    c.getType(),
                    c.getModel(),
                    c.getYear(),
                    c.getColor(),
                    c.getStatus().getGreekLabel()
            });
        }
    }
}
