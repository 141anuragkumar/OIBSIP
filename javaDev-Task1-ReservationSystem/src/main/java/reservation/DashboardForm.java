package reservation;

import javax.swing.*;
import java.awt.*;

/**
  Simple hub shown after a successful login, linking to the
  booking (ReservationForm) and cancellation (CancellationForm) screens.
 */
public class DashboardForm extends JFrame {

    public DashboardForm(String loggedInUser) {
        super("Train Reservation System - Dashboard");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel welcome = new JLabel("Welcome, " + loggedInUser + "!");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("What would you like to do?");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 20, 0));

        JButton bookButton = new JButton("Book a New Ticket");
        JButton cancelButton = new JButton("Cancel a Ticket (by PNR)");
        JButton logoutButton = new JButton("Logout");

        for (JButton b : new JButton[]{bookButton, cancelButton, logoutButton}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(240, 36));
            b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        }

        panel.add(welcome);
        panel.add(subtitle);
        panel.add(bookButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(cancelButton);
        panel.add(Box.createRigidArea(new Dimension(0, 24)));
        panel.add(logoutButton);

        add(panel);

        bookButton.addActionListener(e -> new ReservationForm().setVisible(true));
        cancelButton.addActionListener(e -> new CancellationForm().setVisible(true));
        logoutButton.addActionListener(e -> {
            this.dispose();
            new LoginForm().setVisible(true);
        });
    }
}
