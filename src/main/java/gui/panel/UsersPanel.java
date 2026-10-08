package gui.panel;

import api.AppContext;
import api.model.Employee;
import gui.Ui;
import gui.dialog.EmployeeDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * <h2>Panel Διαχείρισης Χρηστών</h2>
 * Επιτρέπει:
 * <ul>
 *   <li>Προβολή όλων των χρηστών/υπαλλήλων (employees).</li>
 *   <li>Προσθήκη νέου χρήστη.</li>
 *   <li>Διαγραφή χρήστη (με εξαίρεση τον συνδεδεμένο χρήστη).</li>
 * </ul>
 * <p>
 * Η αποθήκευση γίνεται έμμεσα από το {@link api.service.EmployeeService}.
 */
public class UsersPanel extends JPanel {

    private final AppContext ctx;

    private final DefaultTableModel model = TableUtil.nonEditableModel(
            new String[]{"Ονοματεπώνυμο", "Username", "Email"}
    );
    private final JTable table = new JTable(model);

    /**
     * @param ctx application context
     */
    public UsersPanel(AppContext ctx) {
        super(new BorderLayout(10, 10));
        this.ctx = ctx;

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refresh(ctx.employees().findAll());
    }

    /**
     * @return κάτω μπάρα ενεργειών (Ανανέωση/Προσθήκη/Διαγραφή)
     */
    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshBtn = new JButton("Ανανέωση");
        refreshBtn.addActionListener(e -> refresh(ctx.employees().findAll()));

        JButton addBtn = new JButton("Προσθήκη");
        addBtn.addActionListener(e -> onAdd());

        JButton deleteBtn = new JButton("Διαγραφή");
        deleteBtn.addActionListener(e -> onDelete());

        p.add(refreshBtn);
        p.add(addBtn);
        p.add(deleteBtn);
        return p;
    }

    /**
     * Ανοίγει το dialog προσθήκης χρήστη.
     */
    private void onAdd() {
        EmployeeDialog dlg = new EmployeeDialog(SwingUtilities.getWindowAncestor(this), ctx.employees());
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refresh(ctx.employees().findAll());
            Ui.showInfo(this, "Ο χρήστης προστέθηκε.");
        }
    }

    /**
     * Διαγράφει τον επιλεγμένο χρήστη από τον πίνακα (με επιβεβαίωση).
     */
    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Ui.showError(this, "Επιλέξτε χρήστη από τον πίνακα.");
            return;
        }

        String username = String.valueOf(model.getValueAt(row, 1));

        // Business rule (σε επίπεδο GUI): δεν επιτρέπουμε τη διαγραφή του τρέχοντος χρήστη για να μην «κλειδώσει» η εφαρμογή.
        if (username.equalsIgnoreCase(ctx.auth().getCurrent().getUsername())) {
            Ui.showError(this, "Δεν μπορείτε να διαγράψετε τον συνδεδεμένο χρήστη.");
            return;
        }

        if (!Ui.confirm(this, "Να διαγραφεί ο χρήστης: " + username + " ;")) return;

        try {
            ctx.employees().deleteByUsername(username);
            refresh(ctx.employees().findAll());
            Ui.showInfo(this, "Ο χρήστης διαγράφηκε.");
        } catch (Exception ex) {
            Ui.showError(this, ex.getMessage());
        }
    }

    /**
     * Εμφανίζει τους χρήστες στον πίνακα.
     *
     * @param employees λίστα employees
     */
    private void refresh(List<Employee> employees) {
        model.setRowCount(0);
        for (Employee e : employees) {
            model.addRow(new Object[]{e.getFullName(), e.getUsername(), e.getEmail()});
        }
    }
}
