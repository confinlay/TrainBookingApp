package be.kuleuven.distributedsystems.cloud;

import be.kuleuven.distributedsystems.cloud.entities.QuoteConfirmation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.*;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class PubSubService {
    private static final Logger logger = LoggerFactory.getLogger(PubSubService.class);

    public PubSubService() {

    }

    private String projectId() {
        return "ds-part-2";
    }
    private String topicId() {
        return "booking-topic";
    }

    private Publisher publisher() throws IOException {
        // Use the channel to create the Publisher
        return Publisher.newBuilder(TopicName.of(projectId(), topicId()))
                .build();
    }

    public void publishMessage(QuoteConfirmation quoteConfirmation) throws InterruptedException {
        logger.info("PubSubService: publishMessage()");

        Publisher publisher = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String messagePayload = objectMapper.writeValueAsString(quoteConfirmation);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(messagePayload))
                    .build();
            logger.info(pubsubMessage.toString());
            publisher = publisher();
            publisher.publish(pubsubMessage);
        } catch (IOException e) {
            logger.error("Error converting quotes to JSON: " + e.getMessage());
        } finally {
            if (publisher != null) {
                logger.error("Shutting down publisher.");

                publisher.shutdown();
                publisher.awaitTermination(30, TimeUnit.SECONDS);
            }
        }
    }
}
