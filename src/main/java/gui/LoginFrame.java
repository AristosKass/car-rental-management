package gui;

import api.AppContext;
import api.exception.AuthException;
import api.exception.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * <h2>Παράθυρο Σύνδεσης</h2>
 * Απλό Swing {@link JFrame} που επιτρέπει login με username/password.
 * <p>
 * Σημείωση αρχιτεκτονικής: Δεν περιέχει business logic. Η αυθεντικοποίηση γίνεται
 * μέσω {@link api.service.AuthService} από το {@link AppContext}.
 */
public class LoginFrame extends JFrame {

    /** Κοινό context εφαρμογής (services + repositories). */
    private final AppContext ctx;

    /** Πεδίο εισαγωγής username. */
    private final JTextField usernameField = new JTextField(20);

    /** Πεδίο εισαγωγής password (masked). */
    private final JPasswordField passwordField = new JPasswordField(20);

    /**
     * Δημιουργεί το παράθυρο login.
     *
     * @param ctx application context (injected services)
     */
    public LoginFrame(AppContext ctx) {
        super("Car Rentals - Login");
        this.ctx = ctx;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // IMPORTANT (UX/bugfix): Χρησιμοποιούμε pack() αντί για setSize() ώστε τα
        // JTextFields να αποκτούν σωστό preferred size σε όλα τα Look&Feels/OS.
        // Σε κάποιες εγκαταστάσεις (ιδίως Windows + System L&F) χωρίς pack() τα
        // πεδία μπορούν να «συμπιεστούν» και ο χρήστης να νομίζει ότι δεν μπορεί
        // να πληκτρολογήσει.

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Σύνδεση Χρήστη");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        // Labels: χωρίς stretching
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Username:"), gc);

        // Fields: να «απλώνονται» οριζόντια
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(usernameField, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Password:"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(passwordField, gc);

        root.add(form, BorderLayout.CENTER);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> doLogin());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(loginBtn);

        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(loginBtn);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);

        // Defensive: βεβαιώνουμε ότι τα πεδία είναι editable/enabled.
        usernameField.setEditable(true);
        usernameField.setEnabled(true);
        passwordField.setEditable(true);
        passwordField.setEnabled(true);

        // UX/bugfix: Σε ορισμένα περιβάλλοντα, το focus «κολλάει» στο button.
        // Ζητάμε focus πολλαπλές φορές (opened/activated/shown) για μέγιστη συμβατότητα.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                ensureInitialFocus();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                ensureInitialFocus();
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                ensureInitialFocus();
            }
        });
    }

    /**
     * Δίνει focus στο username field με ασφαλή τρόπο.
     * <p>
     * Χρησιμοποιούμε {@link SwingUtilities#invokeLater(Runnable)} ώστε:
     * <ul>
     *   <li>να έχει ολοκληρωθεί το show/realize του παραθύρου,</li>
     *   <li>να αποφύγουμε races που οδηγούν σε "δεν μπορώ να κάνω type".</li>
     * </ul>
     */
    private void ensureInitialFocus() {
        SwingUtilities.invokeLater(() -> {
            // αν δεν είναι ακόμη "showing", δεν κάνουμε τίποτα.
            if (!isShowing()) return;

            // βοηθά στο να έρθει μπροστά το παράθυρο και να πάρει keyboard focus.
            toFront();

            usernameField.requestFocusInWindow();
            usernameField.selectAll();
        });
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            ensureInitialFocus();
        }
    }

    /**
     * Εκτελεί login και ανοίγει το κύριο παράθυρο.
     * <p>
     * Σε περίπτωση αποτυχίας, εμφανίζεται μήνυμα σφάλματος.
     */
    private void doLogin() {
        try {
            ctx.auth().login(usernameField.getText(), new String(passwordField.getPassword()));
            MainFrame main = new MainFrame(ctx, this);
            main.setVisible(true);
            setVisible(false);
        } catch (ValidationException | AuthException ex) {
            Ui.showError(this, ex.getMessage());
        } catch (Exception ex) {
            Ui.showError(this, "Απρόσμενο σφάλμα: " + ex.getMessage());
        }
    }

    /**
     * Καθαρίζει τα πεδία και εμφανίζει ξανά το παράθυρο (χρήσιμο στο logout).
     */
    public void resetAndShow() {
        usernameField.setText("");
        passwordField.setText("");
        setVisible(true);
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }
}
