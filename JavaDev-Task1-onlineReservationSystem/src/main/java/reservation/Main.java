package reservation;

import javax.swing.*;

/*
  Application entry point. Initializes the SQLite database and shows the login screen.
 */
public class Main {
    public static void main(String[] args) {
        DBManager.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default look and feel if system L&F is unavailable.
            }
            new LoginForm().setVisible(true);
        });
    }
}
