package gui;

import api.AppContext;
import gui.panel.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window (after login).
 */
public class MainFrame extends JFrame {

    private final AppContext ctx;
    private final LoginFrame loginFrame;

    public MainFrame(AppContext ctx, LoginFrame loginFrame) {
        super("Car Rentals - Dashboard");
        this.ctx = ctx;
        this.loginFrame = loginFrame;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Οχήματα", new CarsPanel(ctx));
        tabs.addTab("Πελάτες", new CustomersPanel(ctx));
        tabs.addTab("Ενοικίαση", new NewRentalPanel(ctx));
        tabs.addTab("Επιστροφή", new ReturnPanel(ctx));
        tabs.addTab("Ιστορικό", new HistoryPanel(ctx));
        tabs.addTab("Χρήστες", new UsersPanel(ctx));

        JPanel header = new JPanel(new BorderLayout());
        JLabel userLbl = new JLabel("Συνδεδεμένος: " + ctx.auth().getCurrent().getFullName()
                + " (" + ctx.auth().getCurrent().getUsername() + ")");
        userLbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        header.add(userLbl, BorderLayout.WEST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("Μενού");
        JMenuItem logout = new JMenuItem("Αποσύνδεση");
        logout.addActionListener(e -> doLogout());
        JMenuItem exit = new JMenuItem("Έξοδος");
        exit.addActionListener(e -> System.exit(0));
        file.add(logout);
        file.addSeparator();
        file.add(exit);
        mb.add(file);
        return mb;
    }

    private void doLogout() {
        if (!Ui.confirm(this, "Θέλετε σίγουρα αποσύνδεση;")) return;
        ctx.auth().logout();
        dispose();
        loginFrame.resetAndShow();
    }
}
