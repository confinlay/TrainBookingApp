package be.kuleuven.distributedsystems.cloud;

import be.kuleuven.distributedsystems.cloud.auth.SecurityConfiguration;
import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Ticket;
import be.kuleuven.distributedsystems.cloud.entities.Train;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {
    private final Firestore firestore;
    private static final Logger logger = LoggerFactory.getLogger(FirestoreService.class);

    public FirestoreService()  {
        try {
            this.firestore = FirestoreOptions.getDefaultInstance().toBuilder()
                    .setProjectId("ds-part-2")
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .build()
                    .getService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a list of bookings made by the current user from the database
     * @return A list of the bookings
     */
    public List<Booking> getBookings() {
        logger.info("Firestore: getBookings()");

        List<Booking> bookingList = new ArrayList<>();
        try {
            List<QueryDocumentSnapshot> documents = this.firestore
                    .collection("bookings")
                    .whereEqualTo("customer", SecurityConfiguration.getUser().getEmail())
                    .get().get().getDocuments();
            for (DocumentSnapshot document : documents) {
                try {
                    bookingList.add(document.toObject(Booking.class));
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        }
        catch (Exception e){
            logger.error(e.toString());
        }

        return bookingList;
    }

    /**
     * Get a list of all the bookings in the database, irrespective of the user who booked them
     * (requires manager authentication)
     * @return A list of bookings
     */
    public List<Booking> getAllBookings() {
        logger.info("Firestore: getAllBookings()");

        List<Booking> bookingList = new ArrayList<>();
        try {
            List<QueryDocumentSnapshot> documents = this.firestore
                    .collection("bookings")
                    .get().get().getDocuments();
            for (DocumentSnapshot document : documents) {
                try {
                    bookingList.add(document.toObject(Booking.class));
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString());
        }

        return  bookingList;
    }

    /**
     * Get a list of all the trains stored in our own database (i.e. operated by our train company).
     * @return A list of trains.
     */
    public List<Train> getAllTrains() {
        logger.info("Firestore: getAllTrains()");

        List<Train> trainsList = new ArrayList<>();
        try {
            List<QueryDocumentSnapshot> documents = this.firestore
                    .collection("trains")
                    .get().get().getDocuments();
            for (DocumentSnapshot document : documents) {
                try {
                    trainsList.add(document.toObject(Train.class));
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString());
        }
        return trainsList;
    }

    /**
     * Get a list of the available seats for a particular train, at a particular time.
     * @param trainId A unique identifier for the train in question.
     * @param time The departure time of the train selected
     * @return A list of seats.
     */
    public List<Seat> getSeats(String trainId, String time) {
        logger.info("Firestore: getSeats()");

        List<Seat> seatList = new ArrayList<>();
        try {
            List<QueryDocumentSnapshot> documents = this.firestore
                    .collection("trains")
                    .document(trainId)
                    .collection("available_seats")
                    .whereEqualTo("time", time)
                    .get().get().getDocuments();
            for (DocumentSnapshot document : documents) {
                try {
                    seatList.add(document.toObject(Seat.class));
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        }
        catch (Exception e){
            logger.error(e.toString());
        }

        return seatList;
    }

    /**
     * Get a particular seat according to its ID.
     * @param trainId A unique identifier for the train which contains the seat.
     * @param seatId A unique identifier for the seat.
     * @return A seat.
     */
    public Seat getSeat(String trainId, String seatId){
        Seat seat = new Seat();
        try {
             DocumentSnapshot snapshot = this.firestore.collection("trains")
                    .document(trainId)
                    .collection("available_seats")
                    .document(seatId).get().get();
             if(snapshot.exists()){
                 return snapshot.toObject(Seat.class);
             }else{
                 return this.firestore.collection("trains")
                         .document(trainId)
                         .collection("booked_seats")
                         .document(seatId).get().get()
                         .toObject(Seat.class);
             }
        } catch (Exception e){
            logger.error(e.toString());
        }
        return seat;
    }

    /**
     * Get a train by its ID
     * @param trainId A unique identifier for the train.
     * @return A train.
     */
    public Train getTrain(String trainId){
        Train train = new Train();
        try {
            train = this.firestore.collection("trains")
                    .document(trainId).get().get()
                    .toObject(Train.class);
        } catch (Exception e){
            logger.error(e.toString());
        }
        return train;
    }

    /**
     * Get a list of the different departure times available for a particular train.
     * @param trainId A unique identifier for the train.
     * @return A list of times.
     */
    public List<String> getTimes(String trainId){
        logger.info("Firestore: getTimes()");

        List<String> timesList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot =
                    this.firestore.collection("trains")
                            .document(trainId)
                            .collection("times")
                            .get().get();
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                timesList.add(document.getId());
            }
        }
        catch (Exception e) {
            logger.error(e.toString());
        }

        return timesList;
    }

    /**
     * Adds a booking object to the database
     * @param booking An instance of the booking class
     */
    public void addBooking(Booking booking) {
        logger.info("Firestore: addBooking()");

        this.firestore.collection("bookings").document(booking.getId()).set(booking);
    }

    /**
     * Adds a train object to the database
     * @param train An instance of the train class
     */
    public void addTrain(Train train){
        this.firestore.collection("trains").document(train.getTrainId()).set(train);
    }

    /**
     * Adds a seat object to the database, as well as adding its departure time to the
     * times list if it's novel.
     * @param seat An instance of the seat class.
     */
    public void addSeatandTime(Seat seat) {
        this.firestore.collection("trains")
                .document(seat.getTrainId())
                .collection("available_seats")
                .document(seat.getSeatId())
                .set(seat);

        DocumentSnapshot existingTime = null;
        try {
            existingTime = this.firestore.collection("trains")
                    .document(seat.getTrainId())
                    .collection("times")
                    .document(seat.getTime().toString()).get().get();
        } catch (Exception e) {
            logger.error("Error checking whether time exists:", e);
        }

        if (!existingTime.exists()){
            this.firestore.collection("trains")
                    .document(seat.getTrainId())
                    .collection("times")
                    .document(seat.getTime().toString())
                    .set(new HashMap<>());
        }
    }

    /**
     * Issues a ticket to a customer for a train hosted by our internal train company.
     * This consists of checking whether the seat is already booked, and if not, making a new ticket, adding it to the
     * database, and moving the seat in question the available_seats collection to booked_seats.
     * @param trainId A unique identifier for the train in question.
     * @param seatId A unique identifier for the seat in question.
     * @param customer The customers email
     * @param bookingReference A reference for the booking being made
     * @return The new ticket
     */
    public Ticket issueTicket (String trainId, String seatId, String customer, String bookingReference) throws Exception {
        ApiFuture<Object> ticketFuture = firestore.runTransaction(transaction -> {
            DocumentSnapshot ticket_snapshot = this.firestore.collection("trains")
                    .document(trainId)
                    .collection("tickets")
                    .document(seatId)
                    .get().get();

            if(ticket_snapshot.exists()) {
                Ticket old_ticket = ticket_snapshot.toObject(Ticket.class);
                if (Objects.equals(old_ticket.getTicketId(), customer)) {            //if the ticket we want is already assigned to the current customer...
                    revokeTicket(old_ticket.getTrainId(), old_ticket.getSeatId());   //then there has been crash while attempting to add this ticket to a booking, revoke the ticket so that it can be added to this new booking
                } else if (!Objects.equals(old_ticket.getCustomer(), null)) {     //otherwise, if the ticket belongs to another customer
                    throw new Exception("Ticket no longer available");               //throw an exception and do not book the ticket
                }
            }

            Ticket new_ticket = new Ticket(FirestoreInitialiser.internalTrainCompany,
                    trainId,
                    seatId,
                    UUID.randomUUID().toString(),
                    customer,
                    bookingReference);

            this.firestore.collection("trains")
                    .document(trainId)
                    .collection("tickets")
                    .document(seatId)
                    .set(new_ticket);

            Seat old_seat = this.firestore.collection("trains")
                    .document(trainId)
                    .collection("available_seats")
                    .document(seatId)
                    .get().get()
                    .toObject(Seat.class);

            this.firestore.collection("trains")
                    .document(old_seat.getTrainId())
                    .collection("booked_seats")
                    .document(old_seat.getSeatId())
                    .set(old_seat);

            this.firestore.collection("trains")
                    .document(old_seat.getTrainId())
                    .collection("available_seats")
                    .document(old_seat.getSeatId())
                    .delete();
            return new_ticket;
        });
        return (Ticket) ticketFuture.get();
    }

    /**
     * Revokes a ticket previously issued to a customer for a train in our internal train company.
     * Consists of deleting the ticket from the database and moving the corresponding seat from
     * booked_seats to available_seats.
     * @param trainId A unique identifier for the train.
     * @param seatId A unique identifier for the seat.
     */
    public void revokeTicket (String trainId, String seatId) {
        firestore.runTransaction(transaction -> {
           this.firestore.collection("trains")
                        .document(trainId)
                        .collection("tickets")
                        .document(seatId)
                        .delete();

            Seat replacement_seat = this.firestore.collection("trains")
                    .document(trainId)
                    .collection("booked_seats")
                    .document(seatId)
                    .get().get()
                    .toObject(Seat.class);

            this.firestore.collection("trains")
                    .document(replacement_seat.getTrainId())
                    .collection("available_seats")
                    .document(replacement_seat.getSeatId())
                    .set(replacement_seat);

            this.firestore.collection("trains")
                    .document(replacement_seat.getTrainId())
                    .collection("booked_seats")
                    .document(replacement_seat.getSeatId())
                    .delete();

            return null;
        });
    }

    /**
     * Checks if our internal train data has been initialised on the firestore database.
     * It does this by checking if there is anything contained within the trains collection
     * (i.e. whether it exists).
     * @return A boolean indicating whether the data is initialised or not.
     */
    public Boolean isTrainDataInitialised(){
        try {
            QuerySnapshot querySnapshot = this.firestore.collection("trains").get().get();
            logger.info("Firestore: isTrainDataInitialised - " + !querySnapshot.isEmpty());
            return !querySnapshot.isEmpty(); // Data is initialized if the snapshot is not empty
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error checking if train data is initialised: ", e);
            return false;
        }
    }

    /**
     * Checks the number of times a pubsub worker has retried a booking for a particular customer.
     * If the number of retries has not exceeded 3, it increments the counter (stored in the database).
     * Otherwise, it clears the retries and returns true, indicating that there has been too many retries.
     * @param customer The customers email.
     * @return A boolean, indicating whether a pubsub worker has retried more than 3 times for this customer.
     */
    public Boolean checkAndIncrementRetries(String customer){
        try {
            DocumentReference docRef = this.firestore.collection("booking_retries")
                    .document(customer);
            if(docRef.get().get().exists()){
                int retries = docRef.get().get().getLong("retries").intValue();
                if (retries > 3)
                    return true;
                retries++;
                docRef.update("retries", retries);
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("retries", 1);
                docRef.set(data);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Clears the pubsub retries for a particular customer
     * (this needs to be performed by the pubsub worker upon success).
     * @param customer The customers email.
     */
    public void clearRetries(String customer){
        this.firestore.collection("booking_retries")
                .document(customer)
                .delete();
    }

    /**
     * Deletes all the train data in the database
     */
    public void deleteTrainData() {
        Iterable<DocumentReference> docRefs = this.firestore.collection("trains").listDocuments();
        for (DocumentReference docRef : docRefs) {
            Iterable<CollectionReference> collectionReferences = docRef.listCollections();
            for(CollectionReference collectionReference : collectionReferences){
                Iterable<DocumentReference> subDocRefs = collectionReference.listDocuments();
                for (DocumentReference subDocRef : subDocRefs){
                    subDocRef.delete();
                }
            }
            docRef.delete();
        }
    }

    /**
     * Deletes a collection in the database.
     * @param collection The name of the collection to delete.
     */
    public void deleteCollection(String collection){
        Iterable<DocumentReference> documentReferences = this.firestore.collection(collection).listDocuments();
        for (DocumentReference docRef : documentReferences) {
            docRef.delete();
        }
    }
}
