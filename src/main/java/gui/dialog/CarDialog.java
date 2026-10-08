package gui.dialog;

import api.exception.ValidationException;
import api.model.Car;
import api.model.CarStatus;
import api.service.CarService;
import gui.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * <h2>Dialog Προσθήκης/Επεξεργασίας Οχήματος</h2>
 * Swing {@link JDialog} (modal) που επιτρέπει:
 * <ul>
 *   <li>Προσθήκη νέου οχήματος (carOrNull == null)</li>
 *   <li>Επεξεργασία υπάρχοντος οχήματος (carOrNull != null)</li>
 * </ul>
 * <p>
 * Το dialog συλλέγει δεδομένα, φτιάχνει ένα {@link Car} και καλεί το
 * {@link CarService} για add/update. Οι κανόνες εγκυρότητας (π.χ. μοναδικότητα id/πινακίδας)
 * υλοποιούνται στην πλευρά του API.
 */
public class CarDialog extends JDialog {

    private final CarService carService;
    private final boolean editMode;

    // Form fields
    private final JTextField idField = new JTextField(10);
    private final JTextField plateField = new JTextField(12);
    private final JTextField brandField = new JTextField(12);
    private final JTextField typeField = new JTextField(12);
    private final JTextField modelField = new JTextField(12);
    private final JTextField yearField = new JTextField(6);
    private final JTextField colorField = new JTextField(10);
    private final JComboBox<CarStatus> statusBox = new JComboBox<>(CarStatus.values());

    /**
     * Flag που γίνεται {@code true} όταν ο χρήστης πατήσει «Αποθήκευση» και η ενέργεια ολοκληρωθεί επιτυχώς.
     */
    private boolean saved = false;

    /**
     * Δημιουργεί modal dialog προσθήκης/επεξεργασίας.
     *
     * @param owner      γονικό window (για σωστό focus/modality)
     * @param carService service διαχείρισης οχημάτων
     * @param carOrNull  {@code null} για προσθήκη, αλλιώς το όχημα προς επεξεργασία
     */
    public CarDialog(Window owner, CarService carService, Car carOrNull) {
        super(owner,
                (carOrNull == null ? "Προσθήκη Οχήματος" : "Επεξεργασία Οχήματος"),
                ModalityType.APPLICATION_MODAL);
        this.carService = carService;
        this.editMode = (carOrNull != null);

        setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int y = 0;
        addRow(form, gc, y++, "ID:", idField);
        addRow(form, gc, y++, "Πινακίδα:", plateField);
        addRow(form, gc, y++, "Μάρκα:", brandField);
        addRow(form, gc, y++, "Τύπος:", typeField);
        addRow(form, gc, y++, "Μοντέλο:", modelField);
        addRow(form, gc, y++, "Έτος:", yearField);
        addRow(form, gc, y++, "Χρώμα:", colorField);
        addRow(form, gc, y++, "Κατάσταση:", statusBox);

        JButton saveBtn = new JButton("Αποθήκευση");
        JButton cancelBtn = new JButton("Άκυρο");

        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(cancelBtn);
        actions.add(saveBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(actions, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveBtn);

        if (editMode) {
            // Προ-συμπλήρωση πεδίων για επεξεργασία
            idField.setText(String.valueOf(carOrNull.getId()));
            idField.setEnabled(false);
            plateField.setText(carOrNull.getPlate());
            brandField.setText(carOrNull.getBrand());
            typeField.setText(carOrNull.getType());
            modelField.setText(carOrNull.getModel());
            yearField.setText(String.valueOf(carOrNull.getYear()));
            colorField.setText(carOrNull.getColor());
            statusBox.setSelectedItem(carOrNull.getStatus());
        }

        // Pack AFTER adding components (better layout than hard-coded size)
        pack();
        // Τοποθέτηση ΜΕΤΑ το pack για πιο σωστή γεωμετρία/placement.
        setLocationRelativeTo(owner);

        // UX/Fix: σε κάποιες πλατφόρμες το windowOpened δεν αρκεί για focus.
        // Καλούμε το ίδιο logic και στο componentShown.
        Runnable focusJob = this::requestInitialFocus;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(focusJob);
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(focusJob);
            }
        });
    }

    /**
     * Θέτει αρχικό focus στο πρώτο χρήσιμο πεδίο, ώστε ο χρήστης να μπορεί να πληκτρολογήσει άμεσα.
     */
    private void requestInitialFocus() {
        JComponent target = editMode ? plateField : idField;
        target.requestFocusInWindow();
        if (target instanceof JTextField) {
            ((JTextField) target).selectAll();
        }
    }

    /**
     * Προσθέτει μία γραμμή (label + component) στο GridBag form.
     */
    private void addRow(JPanel form, GridBagConstraints gc, int y, String label, JComponent comp) {
        gc.gridx = 0;
        gc.gridy = y;
        gc.weightx = 0;
        form.add(new JLabel(label), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        form.add(comp, gc);
    }

    /**
     * Διαβάζει τα πεδία, κατασκευάζει {@link Car} και καλεί add/update.
     * <p>
     * Σε περίπτωση σφάλματος μορφής (π.χ. μη-αριθμητικό ID) εμφανίζεται μήνυμα.
     */
    private void onSave() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            int year = Integer.parseInt(yearField.getText().trim());

            Car car = new Car(
                    id,
                    plateField.getText().trim(),
                    brandField.getText().trim(),
                    typeField.getText().trim(),
                    modelField.getText().trim(),
                    year,
                    colorField.getText().trim(),
                    (CarStatus) statusBox.getSelectedItem()
            );

            if (editMode) {
                carService.update(car);
            } else {
                carService.add(car);
            }

            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            Ui.showError(this, "ID/Έτος πρέπει να είναι αριθμοί.");
        } catch (ValidationException ex) {
            Ui.showError(this, ex.getMessage());
        } catch (Exception ex) {
            Ui.showError(this, "Απρόσμενο σφάλμα: " + ex.getMessage());
        }
    }

    /**
     * @return {@code true} αν αποθηκεύτηκε επιτυχώς (πατήθηκε Αποθήκευση και πέρασαν οι έλεγχοι)
     */
    public boolean isSaved() {
        return saved;
    }
}
