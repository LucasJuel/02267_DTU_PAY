package main.java.org.g10.services;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import org.g10.utils.StorageHandler;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal consumer that parses the payment payload and stores it using {@link StorageHandler}.
 */
public class PaymentConsumer {

    private final StorageHandler storageHandler;

    public PaymentConsumer() {
        this(StorageHandler.getInstance());
    }

    public PaymentConsumer(StorageHandler storageHandler) {
        this.storageHandler = storageHandler;
    }

    public void handlePaymentRequested(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Payment payload must not be blank");
        }

        Map<String, Object> payment = parsePayload(payload);
        storageHandler.addPayment(payment);
    }

    private Map<String, Object> parsePayload(String payload) {
        try (JsonReader reader = Json.createReader(new StringReader(payload))) {
            JsonObject json = reader.readObject();

            Map<String, Object> payment = new HashMap<>();
            payment.put("customerAccountId", getOptionalString(json, "customerAccountId"));
            payment.put("merchantAccountId", getOptionalString(json, "merchantAccountId"));
            payment.put("amount", getOptionalNumber(json, "amount"));
            payment.put("message", getOptionalString(json, "message"));
            return payment;
        } catch (JsonException e) {
            throw new IllegalArgumentException("Invalid payment payload", e);
        }
    }

    private static String getOptionalString(JsonObject json, String key) {
        if (!json.containsKey(key)) {
            return null;
        }
        JsonValue value = json.get(key);
        return value == JsonValue.NULL ? null : json.getString(key);
    }

    private static Double getOptionalNumber(JsonObject json, String key) {
        if (!json.containsKey(key) || json.isNull(key)) {
            return null;
        }
        return json.getJsonNumber(key).doubleValue();
    }
}
