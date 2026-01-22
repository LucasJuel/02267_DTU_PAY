package org.g10.services;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

import org.g10.DTO.PaymentDTO;
import org.g10.DTO.ReportDTO;
import org.g10.services.ReportingService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;


public class PaymentConsumer implements AutoCloseable {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE_PAYMENT = "payment.requests";
    private static final String DEFAULT_REPORTING_QUEUE = "reporting.requests";

    private final Gson gson = new Gson();
    private final PaymentService paymentService = new PaymentService();
    private final ReportingService reportingService = new ReportingService();

    private final String rabbitHost;
    private final int rabbitPort;
    private final String rabbitUser;
    private final String rabbitPassword;
    private final String queueName;

    private Connection connection;
    private Channel channel;
    public PaymentConsumer() throws IOException, TimeoutException {
        this(
            getEnv("RABBITMQ_HOST", DEFAULT_HOST),
            getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
            getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
            getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
            getEnv("RABBITMQ_QUEUE_PAYMENT", DEFAULT_QUEUE_PAYMENT)
        );
    }

    public PaymentConsumer(String rabbitHost, int rabbitPort, String rabbitUser, String rabbitPassword, String queueName) {
        this.rabbitHost = rabbitHost;
        this.rabbitPort = rabbitPort;
        this.rabbitUser = rabbitUser;
        this.rabbitPassword = rabbitPassword;
        this.queueName = queueName;
    }
    
    public void startListening() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setPort(rabbitPort);
        factory.setUsername(rabbitUser);
        factory.setPassword(rabbitPassword);

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueDeclare(DEFAULT_REPORTING_QUEUE, true, false, false, null);

        System.out.println(" [*] Waiting for payment messages on queue '" + queueName + "'. To exit press CTRL+C");

        DeliverCallback callback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [PAYMENT] Received '" + message + "'");
            try {
                PaymentDTO paymentRequest = gson.fromJson(message, PaymentDTO.class);
                String serviceResponse = paymentService.register(paymentRequest);

                String replyTo = delivery.getProperties().getReplyTo();
                if (replyTo != null && !replyTo.isBlank()) {
                    String correlationId = delivery.getProperties().getCorrelationId();
                    System.out.println("Sending response: " + serviceResponse + " to " + replyTo + " with correlationId " + correlationId);
                    sendResponse(channel, replyTo, correlationId, serviceResponse);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        DeliverCallback reportCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [REPORT REQUEST] Received '" + message + "'");
            try {
                
                ReportDTO reportRequest = gson.fromJson(message, ReportDTO.class);
                System.out.println("Generating report for ID: " + reportRequest.getAccountId());
                List<Map<String, Object>> serviceResponse = reportingService.getAllPayments(reportRequest);

                String replyTo = delivery.getProperties().getReplyTo();
                if (replyTo != null && !replyTo.isBlank()) {
                    String correlationId = delivery.getProperties().getCorrelationId();
                    System.out.println("Sending report response: " + serviceResponse.toString() + " to " + replyTo + " with correlationId " + correlationId);
                    sendResponse(channel, replyTo, correlationId, serviceResponse.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        channel.basicConsume(queueName, true, callback, consumerTag -> { });
        channel.basicConsume(DEFAULT_REPORTING_QUEUE, true, reportCallback, consumerTag -> { });
    }

    /*private final StorageHandler storageHandler;

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
    }*/

    private void sendResponse(Channel channel, String replyTo, String correlationId, String response) throws IOException {
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .contentType("application/json")
                .deliveryMode(2)
                .build();
        channel.basicPublish("", replyTo, props, response.getBytes(StandardCharsets.UTF_8));
    }


    private static String getEnv(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int getEnvInt(String key, int fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void close() throws Exception {
        if (channel != null && channel.isOpen()) channel.close();
        if (connection != null && connection.isOpen()) connection.close();
    }
}
