# Train / Transport Reservation System (Java Swing + JDBC + SQLite)

- java swing - window/button/to creating form
- JDBC       - communicate b/w java and databse(SQLite)
- SQLite     - Database

A desktop GUI application for booking and cancelling train tickets, built with
Java Swing, JDBC, and SQLite (no separate database server required).

## Features (mapped to the requested checklist)

- **Login Form** (LoginForm.java) — username/password fields; shows an
  "Access Denied" dialog for invalid credentials.

- **Reservation Form** (ReservationForm.java) — passenger name, train
  number, train name (auto-populated live as you type a known train number),
  class type (dropdown), date of journey, source and destination station.

- **Book button** — validates the form, generates a unique PNR
  (PRN + 10 random digits, format PNRxxxxxxxxxx), and saves the
  reservation to SQLite.

- **Confirmation dialog** — shows the full booking summary (PNR, passenger,
  train, class, date, route) right after a successful booking.

- **Cancellation Form** (CancellationForm.java) — enter a PNR and click

  **Fetch** to pull up the full booking details from the database.

- **Confirm Cancellation** — guarded by an "Are you sure?" Yes/No dialog;
  only deletes the row from the database on confirmation.

- **Input validation** — no required field may be empty, the train number
  must be numeric, the date must be a real calendar date in yyyy-MM-dd
  format, and the train number must match a train that actually exists
  (so the auto-populated name is meaningful). Source and destination are
  also checked to not be identical.

All SQL uses PreparedStatement with bound parameters — never string
concatenation — to prevent SQL injection.


Database schema (created automatically on first run)

sql
CREATE TABLE users (
    username TEXT PRIMARY KEY,
    password TEXT NOT NULL
);

CREATE TABLE trains (
    train_number TEXT PRIMARY KEY,
    train_name   TEXT NOT NULL
);

CREATE TABLE reservations (
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
);

The database file reservation.db is created in whatever directory you
launch the app from. On first launch, two demo users and six demo trains
are seeded automatically:

- **Demo logins:** admin / admin123  or  user1 / pass123
- **Demo train numbers:** 12301, 12951, 12259, 22691, 12621, 12002
  (type any of these into the Train Number field to see auto-population work)

> Note: passwords are stored in plain text in this demo for simplicity. For
> anything beyond a learning project, hash passwords (e.g. with BCrypt)
> before storing them.

---------------------------------------------------------------------------------------------------

## How to build and run

### Option 1: Maven (recommended — requires internet access to Maven Central)
Important :- If we want to install Maven give them priority other wise choose - option 2

```bash
cd JavaDev-Task1-onlineReservationSystem
mvn clean package
java -jar target/train-reservation-system.jar
```

                                            -------------------------apply 1--------------------------
                                            
mvn package downloads the SQLite JDBC driver and slf4j automatically and
bundles everything into one runnable jar via the shade plugin.

### Option 2: Manual javac/java (jars already included in lib/, no internet needed)

```bash
cd JavaDev-Task1-onlineReservationSystem

# Compile
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar" -OutFile "lib\sqlite-jdbc-3.44.1.0.jar"

# Run
java -cp "out;lib\sqlite-jdbc-3.44.1.0.jar;lib\slf4j-api-1.7.32.jar;lib\slf4j-nop-1.7.32.jar" reservation.Main
```

On Windows, replace : with ; in the -cp classpath argument, and $(find ...)
with a manual list of .java files (or just use the Maven option instead).

## Extending this project

- **Add more trains:** insert rows into the trains table (via a small
  admin screen, or directly with a SQLite browser) — the reservation form
  will auto-populate their names immediately.

- **List/search past bookings:** add a SELECT * FROM reservations WHERE
  passenger_name = ? query and a table (JTable) to browse them.

- **Switch to MySQL:** swap the JDBC URL in DBManager.DB_URL from
  jdbc:sqlite:reservation.db to jdbc:mysql://host:3306/dbname, add the
  MySQL Connector/J dependency, and adjust the CREATE TABLE syntax
  slightly (e.g. AUTO_INCREMENT vs SQLite's INTEGER PRIMARY KEY).
-------------------------------------------------------------------------------------
APPLICATION REVIEW
<img width="1577" height="351" alt="image" src="https://github.com/user-attachments/assets/a7804f92-2497-4620-936c-741b2d325b3b" />
<img width="493" height="353" alt="image" src="https://github.com/user-attachments/assets/c71ff5a9-304b-4712-8fa4-e2ffc03cca98" />
<img width="488" height="342" alt="image" src="https://github.com/user-attachments/assets/56d5496d-bd67-40d3-9789-69d09ff07252" />
<img width="522" height="333" alt="image" src="https://github.com/user-attachments/assets/9dcea673-dfcc-43fa-833c-5d88a3f298ab" />
<img width="526" height="527" alt="image" src="https://github.com/user-attachments/assets/ce0372f0-ccca-4edd-b533-fa20a3fe17a2" />
<img width="517" height="509" alt="image" src="https://github.com/user-attachments/assets/86dd2b2d-70a8-4f35-9bf4-ec157835cca2" />
<img width="531" height="513" alt="image" src="https://github.com/user-attachments/assets/6929dd64-ae5a-4820-8a02-0a8917e55845" />
<img width="544" height="512" alt="image" src="https://github.com/user-attachments/assets/010c86ff-1bae-4027-b50c-9f6e3a3b2130" />
<img width="507" height="494" alt="image" src="https://github.com/user-attachments/assets/cbdc4fb7-59ab-44d7-931c-05c9b9fed93f" />






