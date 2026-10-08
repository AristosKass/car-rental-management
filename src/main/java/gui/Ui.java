package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Μικρές βοηθητικές μέθοδοι Swing για κοινό UI behavior.
 * <p>
 * Η κλάση αυτή συγκεντρώνει τις κλήσεις σε {@link JOptionPane} ώστε:
 * <ul>
 *   <li>να υπάρχει ενιαία εμφάνιση/τίτλοι στα μηνύματα,</li>
 *   <li>να αποφευχθεί επανάληψη κώδικα στα panels/dialogs.</li>
 * </ul>
 */
public final class Ui {

    private Ui() {
        // utility class
    }

    /**
     * Εμφανίζει μήνυμα σφάλματος.
     *
     * @param parent  parent component (για σωστό modality/τοποθέτηση)
     * @param message μήνυμα προς εμφάνιση
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Σφάλμα", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Εμφανίζει ενημερωτικό μήνυμα.
     *
     * @param parent  parent component
     * @param message μήνυμα προς εμφάνιση
     */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Πληροφορία", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Εμφανίζει dialog επιβεβαίωσης (Yes/No).
     *
     * @param parent  parent component
     * @param message ερώτηση προς εμφάνιση
     * @return {@code true} αν ο χρήστης επέλεξε Yes
     */
    public static boolean confirm(Component parent, String message) {
        int res = JOptionPane.showConfirmDialog(parent, message, "Επιβεβαίωση", JOptionPane.YES_NO_OPTION);
        return res == JOptionPane.YES_OPTION;
    }
}
