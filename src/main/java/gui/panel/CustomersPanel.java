package gui.panel;

import api.AppContext;
import api.model.Customer;
import gui.Ui;
import gui.dialog.CustomerDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * <h2>Panel Διαχείρισης Πελατών</h2>
 * Παρέχει:
 * <ul>
 *   <li>Αναζήτηση πελατών (ΑΦΜ, ονοματεπώνυμο, τηλέφωνο).</li>
 *   <li>Προσθήκη νέου πελάτη.</li>
 *   <li>Επεξεργασία υπάρχοντος πελάτη.</li>
 * </ul>
 * <p>
 * Η επιχειρησιακή λογική υλοποιείται στο {@link api.service.CustomerService}.
 */
public class CustomersPanel extends JPanel {

    private final AppContext ctx;

    // Search fields
    private final JTextField afmField = new JTextField(10);
    private final JTextField nameField = new JTextField(12);
    private final JTextField phoneField = new JTextField(10);

    // Results table
    private final DefaultTableModel model = TableUtil.nonEditableModel(
            new String[]{"ΑΦΜ", "Ονοματεπώνυμο", "Τηλέφωνο", "Email"}
    );
    private final JTable table = new JTable(model);

    /**
     * Δημιουργεί το panel και φορτώνει αρχικά όλους τους πελάτες.
     *
     * @param ctx application context
     */
    public CustomersPanel(AppContext ctx) {
        super(new BorderLayout(10, 10));
        this.ctx = ctx;

        add(buildSearchBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refreshTable(ctx.customers().findAll());
    }

    /**
     * @return search bar με κριτήρια + κουμπιά.
     */
    private JComponent buildSearchBar() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Αναζήτηση Πελάτη"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 0;
        p.add(new JLabel("ΑΦΜ:"), gc);
        gc.gridx = 1;
        p.add(afmField, gc);

        gc.gridx = 2;
        p.add(new JLabel("Ονοματεπώνυμο:"), gc);
        gc.gridx = 3;
        p.add(nameField, gc);

        gc.gridx = 4;
        p.add(new JLabel("Τηλέφωνο:"), gc);
        gc.gridx = 5;
        p.add(phoneField, gc);

        JButton searchBtn = new JButton("Αναζήτηση");
        searchBtn.addActionListener(e -> doSearch());

        JButton clearBtn = new JButton("Καθαρισμός");
        clearBtn.addActionListener(e -> {
            afmField.setText("");
            nameField.setText("");
            phoneField.setText("");
            refreshTable(ctx.customers().findAll());
        });

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 6;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(clearBtn);
        actions.add(searchBtn);
        p.add(actions, gc);

        return p;
    }

    /**
     * @return κάτω μπάρα με κουμπιά προσθήκης/επεξεργασίας.
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
     * Εκτελεί αναζήτηση πελατών και ενημερώνει τον πίνακα.
     */
    private void doSearch() {
        List<Customer> res = ctx.customers().search(
                afmField.getText(),
                nameField.getText(),
                phoneField.getText()
        );
        refreshTable(res);
    }

    /**
     * Ανοίγει dialog προσθήκης πελάτη.
     */
    private void onAdd() {
        CustomerDialog dlg = new CustomerDialog(SwingUtilities.getWindowAncestor(this), ctx.customers(), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshTable(ctx.customers().findAll());
            Ui.showInfo(this, "Ο πελάτης προστέθηκε.");
        }
    }

    /**
     * Ανοίγει dialog επεξεργασίας για τον επιλεγμένο πελάτη.
     */
    private void onEdit() {
        String afm = selectedAfm();
        if (afm == null) {
            Ui.showError(this, "Επιλέξτε πελάτη από τον πίνακα.");
            return;
        }
        Customer c = ctx.customers().findByAfm(afm);
        if (c == null) {
            Ui.showError(this, "Ο πελάτης δεν βρέθηκε.");
            return;
        }

        CustomerDialog dlg = new CustomerDialog(SwingUtilities.getWindowAncestor(this), ctx.customers(), c);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshTable(ctx.customers().findAll());
            Ui.showInfo(this, "Ο πελάτης ενημερώθηκε.");
        }
    }

    /**
     * @return το ΑΦΜ του επιλεγμένου πελάτη ή {@code null} αν δεν υπάρχει επιλογή.
     */
    private String selectedAfm() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object v = model.getValueAt(row, 0);
        return v == null ? null : String.valueOf(v);
    }

    /**
     * Εμφανίζει τη δοθείσα λίστα πελατών στον πίνακα.
     *
     * @param customers πελάτες προς εμφάνιση
     */
    private void refreshTable(List<Customer> customers) {
        model.setRowCount(0);
        for (Customer c : customers) {
            model.addRow(new Object[]{c.getAfm(), c.getFullName(), c.getPhone(), c.getEmail()});
        }
    }
}
