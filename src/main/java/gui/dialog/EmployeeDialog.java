package gui.dialog;

import api.exception.ValidationException;
import api.model.Employee;
import api.service.EmployeeService;
import gui.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * <h2>Dialog Προσθήκης Χρήστη/Υπαλλήλου</h2>
 * Modal {@link JDialog} που δημιουργεί νέο {@link Employee} (username/password).
 * <p>
 * Η δημιουργία χρήστη γίνεται μέσω του {@link EmployeeService}.
 */
public class EmployeeDialog extends JDialog {

    private final EmployeeService employeeService;

    // Form fields
    private final JTextField nameField = new JTextField(12);
    private final JTextField surnameField = new JTextField(12);
    private final JTextField usernameField = new JTextField(12);
    private final JTextField emailField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(12);

    /** {@code true} αν ολοκληρώθηκε η αποθήκευση επιτυχώς. */
    private boolean saved = false;

    /**
     * Κατασκευάζει dialog προσθήκης χρήστη.
     *
     * @param owner           γονικό window
     * @param employeeService service διαχείρισης υπαλλήλων
     */
    public EmployeeDialog(Window owner, EmployeeService employeeService) {
        super(owner, "Προσθήκη Χρήστη", ModalityType.APPLICATION_MODAL);
        this.employeeService = employeeService;

        setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int y = 0;
        addRow(form, gc, y++, "Όνομα:", nameField);
        addRow(form, gc, y++, "Επώνυμο:", surnameField);
        addRow(form, gc, y++, "Username:", usernameField);
        addRow(form, gc, y++, "Email:", emailField);
        addRow(form, gc, y++, "Password:", passwordField);

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
     * Θέτει αρχικό focus στο username για άμεση πληκτρολόγηση.
     */
    private void requestInitialFocus() {
        usernameField.requestFocusInWindow();
        usernameField.selectAll();
    }

    /**
     * Προσθέτει label + component σε γραμμή.
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
     * Διαβάζει τα πεδία και προσθέτει τον χρήστη.
     */
    private void onSave() {
        try {
            Employee e = new Employee(
                    nameField.getText().trim(),
                    surnameField.getText().trim(),
                    usernameField.getText().trim(),
                    emailField.getText().trim(),
                    new String(passwordField.getPassword())
            );
            employeeService.add(e);
            saved = true;
            dispose();
        } catch (ValidationException ex) {
            Ui.showError(this, ex.getMessage());
        } catch (Exception ex) {
            Ui.showError(this, "Απρόσμενο σφάλμα: " + ex.getMessage());
        }
    }

    /**
     * @return {@code true} αν έγινε αποθήκευση
     */
    public boolean isSaved() {
        return saved;
    }
}
