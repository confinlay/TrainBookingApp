package be.kuleuven.distributedsystems.cloud.entities;

import java.util.*;

public class Booking {
    private String id;
    private Date time;
    private List<Ticket> tickets;
    private String customer;

    public Booking(){}

    public Booking(String id, Date time, List<Ticket> tickets, String customer) {
        this.id = id;
        this.time = time;
        this.tickets = tickets;
        this.customer = customer;
    }

    public String getId() {
        return this.id;
    }

    public Date getTime() {
        return this.time;
    }

    public List<Ticket> getTickets() {
        return this.tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public String getCustomer() {
        return this.customer;
    }
}
