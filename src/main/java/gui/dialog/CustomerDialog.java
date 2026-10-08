package gui.dialog;

import api.exception.ValidationException;
import api.model.Customer;
import api.service.CustomerService;
import gui.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * <h2>Dialog Προσθήκης/Επεξεργασίας Πελάτη</h2>
 * Modal {@link JDialog} για δημιουργία νέου πελάτη ή ενημέρωση υπάρχοντος.
 * <p>
 * Το dialog συλλέγει στοιχεία (ΑΦΜ, ονοματεπώνυμο, τηλέφωνο, email) και καλεί το
 * {@link CustomerService} για add/update.
 */
public class CustomerDialog extends JDialog {

    private final CustomerService customerService;
    private final boolean editMode;

    // Form fields
    private final JTextField afmField = new JTextField(12);
    private final JTextField nameField = new JTextField(22);
    private final JTextField phoneField = new JTextField(14);
    private final JTextField emailField = new JTextField(22);

    /** {@code true} αν ολοκληρώθηκε επιτυχώς η αποθήκευση. */
    private boolean saved = false;

    /**
     * Κατασκευάζει dialog για προσθήκη/επεξεργασία.
     *
     * @param owner          γονικό window
     * @param customerService service διαχείρισης πελατών
     * @param customerOrNull {@code null} για προσθήκη, αλλιώς πελάτης προς επεξεργασία
     */
    public CustomerDialog(Window owner, CustomerService customerService, Customer customerOrNull) {
        super(owner,
                (customerOrNull == null ? "Προσθήκη Πελάτη" : "Επεξεργασία Πελάτη"),
                ModalityType.APPLICATION_MODAL);
        this.customerService = customerService;
        this.editMode = (customerOrNull != null);

        setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int y = 0;
        addRow(form, gc, y++, "ΑΦΜ:", afmField);
        addRow(form, gc, y++, "Ονοματεπώνυμο:", nameField);
        addRow(form, gc, y++, "Τηλέφωνο:", phoneField);
        addRow(form, gc, y++, "Email:", emailField);

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
            // Προ-συμπλήρωση για επεξεργασία
            afmField.setText(customerOrNull.getAfm());
            afmField.setEnabled(false);
            nameField.setText(customerOrNull.getFullName());
            phoneField.setText(customerOrNull.getPhone());
            emailField.setText(customerOrNull.getEmail());
        }

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
        JComponent target = editMode ? nameField : afmField;
        target.requestFocusInWindow();
        if (target instanceof JTextField) ((JTextField) target).selectAll();
    }

    /**
     * Προσθέτει label + component σε γραμμή του form.
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
     * Δημιουργεί {@link Customer} από τα πεδία και καλεί add/update.
     */
    private void onSave() {
        try {
            Customer c = new Customer(
                    afmField.getText().trim(),
                    nameField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim()
            );

            if (editMode) {
                customerService.update(c);
            } else {
                customerService.add(c);
            }

            saved = true;
            dispose();
        } catch (ValidationException ex) {
            Ui.showError(this, ex.getMessage());
        } catch (Exception ex) {
            Ui.showError(this, "Απρόσμενο σφάλμα: " + ex.getMessage());
        }
    }

    /**
     * @return {@code true} αν έγινε επιτυχής αποθήκευση
     */
    public boolean isSaved() {
        return saved;
    }
}
