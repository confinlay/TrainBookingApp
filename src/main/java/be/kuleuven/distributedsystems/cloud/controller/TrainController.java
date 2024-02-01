package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.FirestoreInitialiser;
import be.kuleuven.distributedsystems.cloud.FirestoreService;
import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Train;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@RestController
@RequestMapping("/api")
public class TrainController {
    private static final String API_KEY = "JViZPgNadspVcHsMbDFrdGg0XXxyiE";
    private static final Logger logger = LoggerFactory.getLogger(TrainController.class);
    private static final String[] trainCompanyUrls = {"https://reliabletrains.com/trains","https://unreliabletrains.com/trains"};
    private final WebClient webClient;
    private final FirestoreService firestoreService;


    public TrainController(WebClient.Builder webClientBuilder, FirestoreService firestoreService) throws IOException {
        this.webClient = webClientBuilder.build();
        this.firestoreService = firestoreService;
    }

    /**
     * Retrieve all train rides from reliable and unreliable sources, as well as from our
     * own internal train company.
     *
     * @return ResponseEntity of Train objects containing trains from both sources.
     */
    @GetMapping("/getTrains")
    public ResponseEntity<List<Train>> getTrains() {
        logger.info("API: getTrains()");

        Collection<Train> allTrains = new ArrayList<>();

        for (String trainCompanyUrl: trainCompanyUrls) {
            Collection<Train> trains = this.webClient.get()
                    .uri(trainCompanyUrl + "?key={key}", API_KEY)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CollectionModel<Train>>() {
                    })
                    .retry(3)
                    .block()
                    .getContent();

            allTrains.addAll(trains);
        }

        // do the same but for our train company
        Collection<Train> trains = firestoreService.getAllTrains();
        allTrains.addAll(trains);

        return ResponseEntity.ok().body(List.of(allTrains.toArray(new Train[0])));
    }

    /**
     * Retrieve train details by trainCompany and train ID.
     *
     * @param trainCompany  The company of the train.
     * @param trainId The ID of the train.
     * @return ResponseEntity of train containing the train details.
     */
    @GetMapping("/getTrain")
    public ResponseEntity<Train> getTrain(String trainCompany, String trainId) {
        logger.info("API: getTrain()");

        Train response = new Train();

        if (Objects.equals(trainCompany,FirestoreInitialiser.internalTrainCompany)){
            response = firestoreService.getTrain(trainId);
        } else {
            response = this.webClient.get()
                    .uri("https://{trainCompany}/trains/{trainId}?key={key}", trainCompany, trainId, API_KEY)
                    .retrieve()
                    .bodyToMono(Train.class)
                    .retry(3)
                    .block();
        }
        return ResponseEntity.ok().body(response);
    }

    /**
     * Retrieve train times by trainCompany and train ID.
     *
     * @param trainCompany  The company of the train.
     * @param trainId The ID of the train.
     * @return ResponseEntity of train times for the given train.
     */
    @GetMapping("/getTrainTimes")
    public String[] getTrainTimes(String trainCompany, String trainId) {
        logger.info("API: getTrainTimes()");
        Collection<String> response = null;
        if (Objects.equals(trainCompany,FirestoreInitialiser.internalTrainCompany)) {
            response = firestoreService.getTimes(trainId);
        } else {
            response = this.webClient.get()
                    .uri("https://{trainCompany}/trains/{trainId}/times?key={key}", trainCompany, trainId, API_KEY)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CollectionModel<String>>() {
                    })
                    .retry(3)
                    .block()
                    .getContent();
        }
        return Arrays.stream(response.toArray(new String[0])).sorted().toArray(String[]::new);
    }

    /**
     * Retrieve available seats for a specific train and time.
     *
     * @param trainCompany  The trainCompany of the train.
     * @param trainId The ID of the train.
     * @param time The specific time for which to retrieve available seats.
     * @return ResponseEntity of available seats for the given train and time.
     */
    @GetMapping("/getAvailableSeats")
    public Map<String, List<Seat>> getAvailableSeats(String trainCompany, String trainId, String time) {
        logger.info("API: getAvailableSeats()");

        List<Seat> seats_list = null;
        if (Objects.equals(trainCompany, FirestoreInitialiser.internalTrainCompany)) {
            seats_list = new ArrayList<>(firestoreService.getSeats(trainId, time));
        } else {
            var response = webClient.get()
                    .uri("https://{trainCompany}/trains/{trainId}/seats?time={time}&available=true&key={key}", trainCompany, trainId, time, API_KEY)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CollectionModel<Seat>>() {
                    })
                    .retry(3)
                    .block()
                    .getContent();

            seats_list = new ArrayList<>(response.stream().toList());
        }

        Collections.sort(seats_list);
        Seat[] seats = seats_list.toArray(new Seat[0]);

        return Arrays.stream(seats).collect(groupingBy(Seat::getType));
    }

    /**
     * Retrieve seat details by trainCompany, train ID, and seat ID.
     *
     * @param trainCompany  The trainCompany of the train.
     * @param trainId The ID of the train.
     * @param seatId   The ID of the seat.
     * @return ResponseEntity of Seat containing seat details.
     */
    @GetMapping("/getSeat")
    public Seat getSeat(String trainCompany, String trainId, String seatId) {
        logger.info("API: getSeat()");

        if(Objects.equals(trainCompany,FirestoreInitialiser.internalTrainCompany)){
            return firestoreService.getSeat(trainId, seatId);
        }

        return webClient.get()
                .uri("https://{trainCompany}/trains/{trainId}/seats/{seatId}?key={key}", trainCompany, trainId, seatId, API_KEY)
                .retrieve()
                .bodyToMono(Seat.class)
                .retry(3)
                .block();
    }
}
