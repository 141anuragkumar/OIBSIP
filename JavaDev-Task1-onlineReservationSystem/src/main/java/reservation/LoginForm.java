package reservation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

/*
  First screen shown to the user. Validates credentials against the
  `users` table and, on success, opens the DashboardForm.
 */
public class LoginForm extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public LoginForm() {
        super("Train Reservation System - Login");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Train / Transport Reservation System");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(16, 8, 8, 8);
        panel.add(loginButton, gbc);

        JLabel hint = new JLabel("<html><i>Demo accounts: admin/admin123 or user1/pass123</i></html>");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 8, 8, 8);
        panel.add(hint, gbc);

        add(panel);

        loginButton.addActionListener(this::handleLogin);
        passwordField.addActionListener(this::handleLogin); // Enter key submits too
        getRootPane().setDefaultButton(loginButton);

        pack();
        setLocationRelativeTo(null);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (ValidationUtils.isBlank(username) || ValidationUtils.isBlank(password)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (DBManager.validateLogin(username, password)) {
                DashboardForm dashboard = new DashboardForm(username);
                dashboard.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Access Denied: invalid username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while logging in:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
