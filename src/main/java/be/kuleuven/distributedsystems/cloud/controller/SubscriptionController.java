package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.FirestoreInitialiser;
import be.kuleuven.distributedsystems.cloud.FirestoreService;
import be.kuleuven.distributedsystems.cloud.SendGridService;
import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.Quote;
import be.kuleuven.distributedsystems.cloud.entities.QuoteConfirmation;
import be.kuleuven.distributedsystems.cloud.entities.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@RestController
public class SubscriptionController {
    private final FirestoreService firestoreService;
    private final WebClient webClient;
    private static final String API_KEY = "JViZPgNadspVcHsMbDFrdGg0XXxyiE";
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    public SubscriptionController(FirestoreService firestoreService, WebClient.Builder webClientBuilder) {
        this.firestoreService = firestoreService;
        this.webClient = webClientBuilder.build();
    }

    /**
     * Handles incoming Pub/Sub messages for quote confirmations.
     * Parses the message body to confirm quotes and create bookings.
     *
     * @param body The JSON string body of the Pub/Sub message containing quote details.
     * @return ResponseEntity indicating the result of the operation.
     *         Returns OK (200) if the processing of quotes and booking creation is successful,
     *         or if a pubsub worker has retried this booking more than 3 times and the booking
     *         must be abandoned.
     *         If any tickets are unavailable, makes calls to cancel any confirmed tickets.
     */
    @PostMapping({"/subscription"})
    public ResponseEntity<Void> handleQuotes(@RequestBody String body) throws IOException {
        logger.info("Subscription Worker: handleQuotes()");

        List<Ticket> confirmedTickets = new ArrayList<>();
        QuoteConfirmation quoteConfirmation = QuoteConfirmation.parsePubSubMessage(body);
        try {
            String bookingReference = UUID.randomUUID().toString();

            for (Quote quote : quoteConfirmation.getQuotes()) {
                Ticket ticket = new Ticket();
                if (Objects.equals(quote.getTrainCompany(), FirestoreInitialiser.internalTrainCompany)){
                    ticket = firestoreService.issueTicket(quote.getTrainId(), quote.getSeatId(), quoteConfirmation.getCustomer(), bookingReference);
                } else {
                    ticket = putSeat(quote.getTrainCompany(), quote.getTrainId(), quote.getSeatId(), quoteConfirmation.getCustomer(), bookingReference);
                }
                confirmedTickets.add(ticket);
            }
            Booking booking = new Booking(bookingReference, Date.from(Instant.now()), confirmedTickets, quoteConfirmation.getCustomer());
            this.firestoreService.addBooking(booking);
            this.firestoreService.clearRetries(booking.getCustomer());
            SendGridService.SendEmail(booking.getCustomer(), true, bookingReference);
            return ResponseEntity.ok().build();
        }catch(Exception e){
            logger.error(e.toString());
            logger.error("ERROR: Re-rolling Tickets");

            for(Ticket ticket: confirmedTickets){
                if (Objects.equals(ticket.getTrainCompany(), FirestoreInitialiser.internalTrainCompany)){
                    firestoreService.revokeTicket(ticket.getTrainId(), ticket.getSeatId());
                } else {
                    cancelTicket(ticket.getTrainCompany(), ticket.getTrainId(), ticket.getSeatId(), ticket.getTicketId());
                }
            }

            Boolean tooManyRetries = firestoreService.checkAndIncrementRetries(quoteConfirmation.getCustomer());
            if(tooManyRetries) {
                this.firestoreService.clearRetries(quoteConfirmation.getCustomer());
                SendGridService.SendEmail(quoteConfirmation.getCustomer(), false,null);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Books a seat for a given quote.
     * Makes a PUT request to a train company to confirm the seat booking.
     *
     * @param trainCompany The name of the train company.
     * @param trainId The ID of the train.
     * @param seatId The ID of the seat to be booked.
     * @param customer The customer's email.
     * @param bookingReference A unique booking reference.
     * @return Ticket The confirmed ticket details.
     *         Retries up to 3 times in case of failures.
     */
    public Ticket putSeat(String trainCompany, String trainId, String seatId, String customer, String bookingReference){
        logger.info("Subscription Worker: putSeat()");

        return webClient.put()
                .uri("https://{trainCompany}/trains/{trainId}/seats/{seatId}/ticket?customer={user}&bookingReference={BookingReference}&key={key}", trainCompany, trainId, seatId, customer, bookingReference, API_KEY)
                .retrieve()
                .bodyToMono(Ticket.class)
                .retry(3)
                .block();
    }

    /**
     * Cancels a ticket for a given seat booking.
     * Makes a DELETE request to a train company to cancel the seat booking.
     *
     * @param trainCompany The name of the train company.
     * @param trainId The ID of the train.
     * @param seatId The ID of the seat for which the booking is to be canceled.
     * @param ticketId The ID of the ticket to be canceled.
     *         Retries up to 3 times in case of failures.
     */
    public void cancelTicket(String trainCompany, String trainId, String seatId, String ticketId) {
        logger.info("Subscription Worker: cancelSeat()");

        webClient.delete()
                .uri("https://{trainCompany}/trains/{trainId}/seats/{seatId}/ticket/{ticketId}?customer={}&bookingReference={}&key={key}", trainCompany, trainId, seatId, ticketId, API_KEY)
                .retrieve()
                .bodyToMono(Ticket.class)
                .retry(3)
                .block();
    }
}
