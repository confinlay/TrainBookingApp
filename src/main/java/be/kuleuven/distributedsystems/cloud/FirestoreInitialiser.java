package be.kuleuven.distributedsystems.cloud;

import be.kuleuven.distributedsystems.cloud.controller.TrainController;
import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Train;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.time.LocalDateTime.parse;

@Service
public class FirestoreInitialiser {

    private final FirestoreService firestoreService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(FirestoreInitialiser.class);
    public static final String internalTrainCompany = "Conor and Mikkel's Train Company";


    public FirestoreInitialiser(FirestoreService firestoreService, ObjectMapper objectMapper) {
        this.firestoreService = firestoreService;
        this.objectMapper = objectMapper;
    }

    /**
     * If there is no train data in the firestore database, this function performs a clean delete of
     * all data in the database, and then initialises new data according to data.json.
     */
    @PostConstruct
    public void init() throws IOException {
        logger.info("Firestore: initialising train data");

        // Checks if there's already train data initialised
        if(firestoreService.isTrainDataInitialised()) return;
        // If not, we ensure that all existing data is deleted from the firebase before initialising our new data
        firestoreService.deleteTrainData();
        firestoreService.deleteCollection("bookings");
        firestoreService.deleteCollection("booking_retries");

//         Read and parse data.json
        String jsonData;
        try (InputStream is = FirestoreInitialiser.class.getClassLoader().getResourceAsStream("data.json")) {
            if (is == null) {
                throw new Exception("Resource 'data.json' not found in classpath");
            }
            jsonData = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            JsonNode rootNode = objectMapper.readTree(jsonData);
            JsonNode trainsNode = rootNode.path("trains");

            for (JsonNode trainNode : trainsNode) {
                Train train = createTrainFromJsonNode(trainNode);
                firestoreService.addTrain(train);

                JsonNode seatsNode = trainNode.path("seats");
                for (JsonNode seatNode : seatsNode) {
                    Seat seat = createSeatFromJsonNode(seatNode, train);
                    firestoreService.addSeatandTime(seat);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a Train object from a JsonNode object.
     * @param trainNode A JsonNode containing information about a train.
     * @return An instance of the train class, with information corresponding to that which was provided by the JsonNode.
     */
    private static Train createTrainFromJsonNode(JsonNode trainNode) {
        String TrainID = UUID.randomUUID().toString();
        String Name = trainNode.path("name").asText();
        String Location = trainNode.path("location").asText();
        String Image = trainNode.path("image").asText();
        return new Train(internalTrainCompany, TrainID, Name, Location, Image);
    }

    /**
     * Creates a Seat object from a JsonNode object.
     * @param seatNode A JsonNode containing information about a Seat
     * @param train The train object to which this seat will belong.
     * @return An instance of the Seat class, with information corresponding to that which was provided by the JsonNode.
     */
    private static Seat createSeatFromJsonNode(JsonNode seatNode, Train train) {
        String TrainCompany = train.getTrainCompany();
        String TrainId = train.getTrainId();
        String SeatId = UUID.randomUUID().toString();
        String time = seatNode.path("time").asText();
        String type = seatNode.path("type").asText();
        String name = seatNode.path("name").asText();
        double price = seatNode.path("price").asDouble();
        return new Seat(TrainCompany, TrainId, SeatId, time, type, name, price);
    }
}