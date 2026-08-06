package reservation;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 Cancellation screen: user enters a PNR, clicks Fetch to look up and
 display the full booking, then Confirm Cancellation
 */
public class CancellationForm extends JFrame {

    private final JTextField pnrField = new JTextField(20);
    private final JTextArea detailsArea = new JTextArea(10, 30);
    private final JButton cancelButton = new JButton("Confirm Cancellation");

    private Reservation currentReservation; // last fetched reservation, if any

    public CancellationForm() {
        super("Cancel a Reservation");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Cancel a Reservation");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        topPanel.add(new JLabel("PNR Number:"));
        topPanel.add(pnrField);
        JButton fetchButton = new JButton("Fetch");
        topPanel.add(fetchButton);

        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        detailsArea.setText("Enter a PNR and click Fetch to see booking details here.");
        JScrollPane scrollPane = new JScrollPane(detailsArea);

        cancelButton.setEnabled(false);
        JButton closeButton = new JButton("Close");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottomPanel.add(cancelButton);
        bottomPanel.add(closeButton);

        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.add(title, BorderLayout.NORTH);
        northWrapper.add(topPanel, BorderLayout.CENTER);

        panel.add(northWrapper, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);

        fetchButton.addActionListener(e -> handleFetch());
        cancelButton.addActionListener(e -> handleCancellation());
        closeButton.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(null);
    }

    private void handleFetch() {
        String pnr = pnrField.getText().trim();

        if (ValidationUtils.isBlank(pnr)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a PNR number.", "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Reservation reservation = DBManager.fetchReservationByPnr(pnr);
            if (reservation == null) {
                detailsArea.setText("No booking found for PNR: " + pnr);
                currentReservation = null;
                cancelButton.setEnabled(false);
            } else {
                detailsArea.setText(reservation.toDisplayString());
                currentReservation = reservation;
                cancelButton.setEnabled(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while fetching: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancellation() {
        if (currentReservation == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this booking?\n\nPNR: " + currentReservation.pnr()
                        + "\nPassenger: " + currentReservation.passengerName(),
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted = DBManager.deleteReservationByPnr(currentReservation.pnr());
            if (deleted) {
                JOptionPane.showMessageDialog(this,
                        "Booking with PNR " + currentReservation.pnr() + " has been cancelled.",
                        "Cancellation Successful", JOptionPane.INFORMATION_MESSAGE);
                detailsArea.setText("Enter a PNR and click Fetch to see booking details here.");
                pnrField.setText("");
                currentReservation = null;
                cancelButton.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Booking could not be found (it may have already been cancelled).",
                        "Cancellation Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while cancelling: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
