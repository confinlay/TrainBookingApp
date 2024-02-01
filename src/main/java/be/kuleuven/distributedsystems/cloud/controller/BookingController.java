package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.FirestoreService;
import be.kuleuven.distributedsystems.cloud.PubSubService;
import be.kuleuven.distributedsystems.cloud.auth.SecurityConfiguration;
import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.Quote;
import be.kuleuven.distributedsystems.cloud.entities.QuoteConfirmation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final FirestoreService firestoreService;
    private final PubSubService pubSubService;
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    public BookingController(FirestoreService firestoreService, PubSubService pubSubService) {
        this.firestoreService = firestoreService;
        this.pubSubService = pubSubService;
    }

    /**
     * Retrieves the current user's bookings
     *
     * @return ResponseEntity containing a list of Booking objects.
     *         The response is OK (200) if the request is successful.
     */
    @GetMapping("/getBookings")
    public ResponseEntity<List<Booking>> getBookings(){
        logger.info("Booking API: getBookings()");

        return ResponseEntity.ok(firestoreService.getBookings());
    }

    /**
     * Retrieves all bookings from the database, irrespective of the current user.
     * This endpoint is restricted to users with manager roles ({"roles" : ["manager"]} in firebase authentication).
     *
     * @return ResponseEntity containing a list of all Booking objects.
     *         Returns null if the user does not have manager privileges.
     *         The response is OK (200) if the request is successful.
     */
    @GetMapping("/getAllBookings")
    public ResponseEntity<List<Booking>> getAllBookings(){
        if(!SecurityConfiguration.getUser().isManager()) return null;
        logger.info("Booking API: getAllBookings()");

        return ResponseEntity.ok(firestoreService.getAllBookings());
    }

    /**
     * Retrieves the best customers based on the number of tickets booked.
     * This endpoint is restricted to manager roles.
     *
     * @return ResponseEntity containing an array of Strings representing the best customers' emails.
     *         Returns an empty array if no bookings are found.
     *         Access is restricted to users with manager roles.
     */
    @GetMapping("/getBestCustomers")
    public ResponseEntity<String[]> getBestCustomers(){
        if(!SecurityConfiguration.getUser().isManager()) return null;
        logger.info("Booking API: getBestCustomers()");

        List<Booking> bookings = firestoreService.getAllBookings();
        if(bookings.isEmpty()) return ResponseEntity.ok(new String[]{});

        Booking maxBooking = bookings.stream().max(Comparator.comparing(booking -> booking.getTickets().size())).orElseThrow(NoSuchElementException::new);
        Stream<Booking> maxBookings = bookings.stream().filter(booking -> booking.getTickets().size() == maxBooking.getTickets().size());
        return ResponseEntity.ok(maxBookings.map(Booking::getCustomer).distinct().toArray(String[]::new));
    }

    /**
     * Attempts to confirm all the quotes in a user's cart.
     * The quotes are received as an array in the request body.
     * This endpoint publishes a message to the Pub/Sub service to request quote confirmation.
     *
     * @param quotes An array of Quote objects to be confirmed.
     */
    @PostMapping("/confirmQuotes")
    public void confirmQuotes(@RequestBody Quote[] quotes) throws InterruptedException {
        logger.info("Booking API: confirmQuotes()");
        QuoteConfirmation quoteConfirmation = new QuoteConfirmation(SecurityConfiguration.getUser().getEmail(), Arrays.stream(quotes).toList());
        pubSubService.publishMessage(quoteConfirmation);
    }
}
