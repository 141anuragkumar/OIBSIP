package reservation;

/*
  Immutable value object representing one row of the reservations table.
 */
public record Reservation(
        String pnr,
        String passengerName,
        String trainNumber,
        String trainName,
        String classType,
        String journeyDate,
        String sourceStation,
        String destinationStation,
        String bookingTimestamp
) {
    /* Multi-line, human-readable summary used in confirmation/fetch dialogs. */
    public String toDisplayString() {
        return """
                PNR Number       : %s
                Passenger Name   : %s
                Train Number     : %s
                Train Name       : %s
                Class            : %s
                Date of Journey  : %s
                Source Station   : %s
                Destination      : %s
                Booked On        : %s
                """.formatted(pnr, passengerName, trainNumber, trainName, classType,
                journeyDate, sourceStation, destinationStation, bookingTimestamp);
    }
}
