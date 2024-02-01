package be.kuleuven.distributedsystems.cloud.entities;

public class Ticket {
    private String trainCompany;
    private String trainId;
    private String seatId;
    private String ticketId;
    private String customer;
    private String bookingReference;

    public Ticket() {
    }

    public Ticket(String trainCompany, String trainId, String seatId, String ticketId, String customer, String bookingReference) {
        this.trainCompany = trainCompany;
        this.trainId = trainId;
        this.seatId = seatId;
        this.ticketId = ticketId;
        this.customer = customer;
        this.bookingReference = bookingReference;
    }

    public String getTrainCompany() {
        return trainCompany;
    }

    public String getTrainId() {
        return trainId;
    }

    public String getSeatId() {
        return this.seatId;
    }

    public String getTicketId() {
        return this.ticketId;
    }

    public String getCustomer() {
        return this.customer;
    }

    public String getBookingReference() {
        return this.bookingReference;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Ticket)) {
            return false;
        }
        var other = (Ticket) o;
        return this.ticketId.equals(other.ticketId)
                && this.seatId.equals(other.seatId)
                && this.trainId.equals(other.trainId)
                && this.trainCompany.equals(other.trainCompany);
    }

    @Override
    public int hashCode() {
        return this.trainCompany.hashCode() * this.trainId.hashCode() * this.seatId.hashCode() * this.ticketId.hashCode();
    }
}
