package reservation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.SecureRandom;


public final class DBManager {

    // reservation.db will be created in the directory the app is launched from.
    private static final String DB_URL = "jdbc:sqlite:reservation.db";

    private static Connection connection;

    static {
        // Explicitly register the driver. Some environment
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "SQLite JDBC driver not found on classpath. "
                            + "Make sure sqlite-jdbc-*.jar is included (see lib/ folder / pom.xml).", e);
        }
    }

    private DBManager() {
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement pragma = connection.createStatement()) {
                // Keep referential-integrity style checks on; SQLite defaults them off.
                pragma.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    /**
      Creates tables if they do not already exist and inserts demo data
      the very first time the database file is created.
     */
    public static void initializeDatabase() {
        try {
            Connection conn = getConnection();

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        username TEXT PRIMARY KEY,
                        password TEXT NOT NULL
                    )
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS trains (
                        train_number TEXT PRIMARY KEY,
                        train_name   TEXT NOT NULL
                    )
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reservations (
                        pnr                 TEXT PRIMARY KEY,
                        passenger_name      TEXT NOT NULL,
                        train_number        TEXT NOT NULL,
                        train_name          TEXT NOT NULL,
                        class_type          TEXT NOT NULL,
                        journey_date        TEXT NOT NULL,
                        source_station      TEXT NOT NULL,
                        destination_station TEXT NOT NULL,
                        booking_timestamp   TEXT NOT NULL,
                        FOREIGN KEY (train_number) REFERENCES trains(train_number)
                    )
                """);
            }

            seedDemoDataIfEmpty(conn);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    private static void seedDemoDataIfEmpty(Connection conn) throws SQLException {
        // Seed users only if the users table is empty.
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (username, password) VALUES (?, ?)")) {
                    ps.setString(1, "admin");
                    ps.setString(2, "admin123");
                    ps.addBatch();

                    ps.setString(1, "user1");
                    ps.setString(2, "pass123");
                    ps.addBatch();

                    ps.executeBatch();
                }
            }
        }

        // Seed a handful of sample trains so train-number auto-populate works out of the box.
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM trains")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO trains (train_number, train_name) VALUES (?, ?)")) {
                    String[][] demoTrains = {
                            {"12301", "ayodhya dham Express"},
                            {"12951", "Mumbai Rajdhani Express"},
                            {"12259", "Sealdah Duronto Express"},
                            {"22691", "Bangalore Rajdhani Express"},
                            {"12621", "Tamil Nadu Express"},
                            {"12002", "Bhopal Shatabdi Express"},
                    };
                    for (String[] t : demoTrains) {
                        ps.setString(1, t[0]);
                        ps.setString(2, t[1]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Authentication section 

    /* Returns true only if username exists and the password matches exactly. */
    public static boolean validateLogin(String username, String password) throws SQLException {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    return storedPassword.equals(password);
                }
                return false;
            }
        }
    }

    // ---------------------------------------------------------------
    // Trains

    /* Looks up a train name by train number. Returns null if not found. */
    public static String lookupTrainName(String trainNumber) throws SQLException {
        String sql = "SELECT train_name FROM trains WHERE train_number = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("train_name") : null;
            }
        }
    }

    // ---------------------------------------------------------------
    // Reservations

    public static void insertReservation(Reservation r) throws SQLException {
        String sql = """
            INSERT INTO reservations
            (pnr, passenger_name, train_number, train_name, class_type,
             journey_date, source_station, destination_station, booking_timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, r.pnr());
            ps.setString(2, r.passengerName());
            ps.setString(3, r.trainNumber());
            ps.setString(4, r.trainName());
            ps.setString(5, r.classType());
            ps.setString(6, r.journeyDate());
            ps.setString(7, r.sourceStation());
            ps.setString(8, r.destinationStation());
            ps.setString(9, r.bookingTimestamp());
            ps.executeUpdate();
        }
    }

    public static Reservation fetchReservationByPnr(String pnr) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE pnr = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Reservation(
                        rs.getString("pnr"),
                        rs.getString("passenger_name"),
                        rs.getString("train_number"),
                        rs.getString("train_name"),
                        rs.getString("class_type"),
                        rs.getString("journey_date"),
                        rs.getString("source_station"),
                        rs.getString("destination_station"),
                        rs.getString("booking_timestamp")
                );
            }
        }
    }

    /** Returns true if a row was actually deleted. */
    public static boolean deleteReservationByPnr(String pnr) throws SQLException {
        String sql = "DELETE FROM reservations WHERE pnr = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, pnr);
            return ps.executeUpdate() > 0;
        }
    }

    /*
      Generates a unique 10-digit PNR of the form "PNR" + 10 digits,
      re-rolling in the (extremely unlikely) case of a collision.
     */
    public static String generateUniquePnr() throws SQLException {
        SecureRandom random = new SecureRandom();
        String candidate;
        do {
            StringBuilder digits = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                digits.append(random.nextInt(10));
            }
            candidate = "PNR" + digits;
        } while (fetchReservationByPnr(candidate) != null);
        return candidate;
    }
}
