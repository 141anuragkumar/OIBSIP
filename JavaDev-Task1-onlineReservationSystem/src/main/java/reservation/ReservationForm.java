package reservation;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
  Booking screen: collects passenger/journey details, auto-populates the
  train name as soon as a known train number is typed, validates input,
  inserts the reservation with a freshly generated PNR, and shows a
  confirmation dialog with the full booking summary.
 */
public class ReservationForm extends JFrame {

    private final JTextField passengerNameField = new JTextField(20);
    private final JTextField trainNumberField = new JTextField(20);
    private final JTextField trainNameField = new JTextField(20);
    private final JComboBox<String> classTypeCombo = new JComboBox<>(new String[]{
            "Sleeper (SL)", "AC 3 Tier (3A)", "AC 2 Tier (2A)", "AC First Class (1A)",
            "Chair Car (CC)", "General (GEN)"
    });
    private final JTextField dateField = new JTextField(20);
    private final JTextField sourceField = new JTextField(20);
    private final JTextField destinationField = new JTextField(20);
    private final JLabel trainLookupStatus = new JLabel(" ");

    public ReservationForm() {
        super("Book a New Ticket");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        JLabel title = new JLabel("New Reservation");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(title, gbc);
        gbc.gridwidth = 1;

        row = addRow(panel, gbc, row, "Passenger Name:", passengerNameField);
        row = addRow(panel, gbc, row, "Train Number:", trainNumberField);

        trainNameField.setEditable(false);
        trainNameField.setBackground(new Color(240, 240, 240));
        row = addRow(panel, gbc, row, "Train Name (auto):", trainNameField);

        gbc.gridx = 1;
        gbc.gridy = row++;
        trainLookupStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        panel.add(trainLookupStatus, gbc);

        row = addRow(panel, gbc, row, "Class Type:", classTypeCombo);

        dateField.setToolTipText("Format: " + ValidationUtils.DATE_PATTERN);
        row = addRow(panel, gbc, row, "Date of Journey (yyyy-MM-dd):", dateField);

        row = addRow(panel, gbc, row, "Source Station:", sourceField);
        row = addRow(panel, gbc, row, "Destination Station:", destinationField);

        JButton bookButton = new JButton("Book Ticket");
        bookButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        JButton closeButton = new JButton("Close");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.add(bookButton);
        buttonPanel.add(closeButton);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        panel.add(buttonPanel, gbc);

        add(panel);

        // Auto-populate train name as the user types the train number.
        trainNumberField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { lookupTrainName(); }
            @Override public void removeUpdate(DocumentEvent e) { lookupTrainName(); }
            @Override public void changedUpdate(DocumentEvent e) { lookupTrainName(); }
        });

        bookButton.addActionListener(e -> handleBooking());
        closeButton.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(null);
    }

    private int addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(field, gbc);

        return row + 1;
    }

    private void lookupTrainName() {
        String trainNumber = trainNumberField.getText().trim();
        if (trainNumber.isEmpty()) {
            trainNameField.setText("");
            trainLookupStatus.setText(" ");
            return;
        }
        if (!ValidationUtils.isNumeric(trainNumber)) {
            trainNameField.setText("");
            trainLookupStatus.setText("Train number must be numeric.");
            trainLookupStatus.setForeground(Color.RED);
            return;
        }
        try {
            String name = DBManager.lookupTrainName(trainNumber);
            if (name != null) {
                trainNameField.setText(name);
                trainLookupStatus.setText("Train found.");
                trainLookupStatus.setForeground(new Color(0, 128, 0));
            } else {
                trainNameField.setText("");
                trainLookupStatus.setText("No train found with this number.");
                trainLookupStatus.setForeground(Color.RED);
            }
        } catch (SQLException e) {
            trainNameField.setText("");
            trainLookupStatus.setText("Lookup error: " + e.getMessage());
            trainLookupStatus.setForeground(Color.RED);
        }
    }

    private void handleBooking() {
        String passengerName = passengerNameField.getText().trim();
        String trainNumber = trainNumberField.getText().trim();
        String trainName = trainNameField.getText().trim();
        String classType = (String) classTypeCombo.getSelectedItem();
        String journeyDate = dateField.getText().trim();
        String source = sourceField.getText().trim();
        String destination = destinationField.getText().trim();

        // --- Required-field validation ---
        if (ValidationUtils.isBlank(passengerName) || ValidationUtils.isBlank(trainNumber)
                || ValidationUtils.isBlank(journeyDate) || ValidationUtils.isBlank(source)
                || ValidationUtils.isBlank(destination)) {
            showError("Please fill in all required fields.");
            return;
        }

        // --- Numeric train number ---
        if (!ValidationUtils.isNumeric(trainNumber)) {
            showError("Train number must contain digits only.");
            return;
        }

        // --- Train must actually exist so the auto-populated name is valid ---
        if (ValidationUtils.isBlank(trainName)) {
            showError("No train found for train number " + trainNumber
                    + ". Please enter a valid, registered train number.");
            return;
        }

        // --- Date format ---
        if (!ValidationUtils.isValidDate(journeyDate)) {
            showError("Date of Journey must be a valid date in " + ValidationUtils.DATE_PATTERN + " format.");
            return;
        }

        // --- Source/destination sanity check ---
        if (source.equalsIgnoreCase(destination)) {
            showError("Source and destination stations cannot be the same.");
            return;
        }

        try {
            String pnr = DBManager.generateUniquePnr();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            Reservation reservation = new Reservation(
                    pnr, passengerName, trainNumber, trainName, classType,
                    journeyDate, source, destination, timestamp
            );

            DBManager.insertReservation(reservation);

            JOptionPane.showMessageDialog(this,
                    "Booking Confirmed!\n\n" + reservation.toDisplayString(),
                    "Reservation Successful", JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (SQLException ex) {
            showError("Database error while booking: " + ex.getMessage());
        }
    }

    private void clearForm() {
        passengerNameField.setText("");
        trainNumberField.setText("");
        trainNameField.setText("");
        classTypeCombo.setSelectedIndex(0);
        dateField.setText("");
        sourceField.setText("");
        destinationField.setText("");
        trainLookupStatus.setText(" ");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}
