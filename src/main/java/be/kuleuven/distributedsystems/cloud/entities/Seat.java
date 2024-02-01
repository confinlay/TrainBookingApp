package be.kuleuven.distributedsystems.cloud.entities;

import java.util.Date;

public class Seat implements Comparable<Seat> {
    private String trainCompany;
    private String trainId;
    private String seatId;
    private String time;
    private String type;
    private String name;
    private double price;

    public Seat() {
    }

    public Seat(String trainCompany, String trainId, String seatId, String time, String type, String name, double price) {
        this.trainCompany = trainCompany;
        this.trainId = trainId;
        this.seatId = seatId;
        this.time = time;
        this.type = type;
        this.name = name;
        this.price = price;
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

    public String getTime() {
        return this.time;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public double getPrice() {
        return this.price;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Seat)) {
            return false;
        }
        var other = (Seat) o;
        return this.trainCompany.equals(other.trainCompany)
                && this.trainId.equals(other.trainId)
                && this.seatId.equals(other.seatId);
    }

    @Override
    public int hashCode() {
        return this.trainCompany.hashCode() * this.trainId.hashCode() * this.seatId.hashCode();
    }

    @Override
    public int compareTo(Seat seat){
        String this_seat_name = this.getName();
        int this_seat_number = Integer.parseInt(this_seat_name.substring(0, this_seat_name.length() - 1));
        char this_seat_letter = this_seat_name.charAt(this_seat_name.length() - 1);

        String given_seat_name = seat.getName();
        int given_seat_number = Integer.parseInt(given_seat_name.substring(0, given_seat_name.length() - 1));
        char given_seat_letter = given_seat_name.charAt(given_seat_name.length() - 1);

        if (this_seat_number < given_seat_number){
            return -1;
        } else if (this_seat_number > given_seat_number){
            return 1;
        } else if (this_seat_letter > given_seat_letter){
            return 1;
        } else if (this_seat_letter < given_seat_letter){
            return -1;
        } else {
            return 0;
        }
    }
}
