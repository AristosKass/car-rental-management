package gui;

import api.AppContext;

import javax.swing.*;

/**
 * <h2>Entry point</h2>
 * Μοναδική κλάση με {@code main} (όπως απαιτεί η εκφώνηση).
 * <p>
 * Η εκκίνηση γίνεται στο Swing Event Dispatch Thread (EDT) με {@link SwingUtilities#invokeLater(Runnable)}
 * ώστε όλες οι Swing ενημερώσεις να είναι thread-safe.
 */
public final class App {

    private App() {
        // no instances
    }

    /**
     * Εκκινεί την εφαρμογή.
     *
     * @param args arguments γραμμής εντολών (δεν χρησιμοποιούνται)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // System Look & Feel για καλύτερη ενσωμάτωση.
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            // Δημιουργία context (repositories/services) και εμφάνιση login.
            AppContext ctx = new AppContext();
            LoginFrame login = new LoginFrame(ctx);
            login.setVisible(true);
        });
    }
}
