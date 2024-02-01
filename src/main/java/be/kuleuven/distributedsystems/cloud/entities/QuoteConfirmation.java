package be.kuleuven.distributedsystems.cloud.entities;

import com.google.gson.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class QuoteConfirmation {
    private String customer;
    private List<Quote> quotes;

    public QuoteConfirmation(){}

    public QuoteConfirmation(String customer, List<Quote> quotes) {
        this.customer = customer;
        this.quotes = quotes;
    }

    public String getCustomer() {
        return this.customer;
    }

    public List<Quote> getQuotes() {
        return this.quotes;
    }

    public static QuoteConfirmation parsePubSubMessage(String pubSubMessage) {
        // Parse the base64-encoded JSON array from the Pub/Sub message
        String jsonPayload = decodeBase64(pubSubMessage);
        JsonObject jsonObject = JsonParser.parseString(jsonPayload).getAsJsonObject();

        // Map to object
        return new Gson().fromJson(jsonObject, QuoteConfirmation.class);
    }

    private static String decodeBase64(String body) {
        JsonElement jsonElement = JsonParser.parseString(body);

        // Check if the root element is a JsonObject
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Check if the "message" field is present and is a JsonObject
            if (jsonObject.has("message") && jsonObject.get("message").isJsonObject()) {
                JsonObject messageObject = jsonObject.getAsJsonObject("message");

                // Check if the "data" field is present
                if (messageObject.has("data")) {
                    String base64Data = messageObject.get("data").getAsString();
                    byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
                    return new String(decodedBytes, StandardCharsets.UTF_8);
                } else {
                    throw new IllegalArgumentException("Missing 'data' field in the 'message' object.");
                }
            } else {
                throw new IllegalArgumentException("Missing or invalid 'message' field in the JSON object.");
            }
        } else {
            throw new IllegalArgumentException("Invalid JSON format: Expected a JSON object.");
        }
    }
}
