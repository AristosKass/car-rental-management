package gui.panel;

import javax.swing.table.DefaultTableModel;

/**
 * Βοηθητική κλάση για tables.
 * <p>
 * Η Swing {@link javax.swing.JTable} από προεπιλογή επιτρέπει επεξεργασία κελιών.
 * Εδώ παρέχουμε έναν {@link DefaultTableModel} όπου όλα τα κελιά είναι read-only
 * (όπως απαιτείται για εμφάνιση δεδομένων χωρίς edit-in-place).
 */
public final class TableUtil {

    private TableUtil() {
        // utility class
    }

    /**
     * Δημιουργεί {@link DefaultTableModel} με τα δοθέντα ονόματα στηλών και μηδενικές αρχικές γραμμές,
     * όπου η επεξεργασία κελιών είναι απενεργοποιημένη.
     *
     * @param columns ονόματα στηλών (header)
     * @return read-only table model
     */
    public static DefaultTableModel nonEditableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
