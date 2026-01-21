package org.g10.Services;

import com.rabbitmq.client.*;
import com.rabbitmq.client.ConnectionFactory;
import com.google.gson.Gson;
import org.g10.DTO.TokenDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class TokenConsumer implements AutoCloseable {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE_TOKEN = "token.requests";

    private final String rabbitHost;
    private final int rabbitPort;
    private final String rabbitUser;
    private final String rabbitPassword;
    private final String tokenQueue;

    private final Gson gson = new Gson();
    private final TokenService tokenService = new TokenService();
    private Connection connection;
    private Channel channel;
    public TokenConsumer() throws IOException, TimeoutException {
        this(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                getEnv("RABBITMQ_QUEUE_MERCHANT", DEFAULT_QUEUE_TOKEN)
        );
    }

    public TokenConsumer(String rabbitmqHost, int rabbitmqPort, String rabbitmqUser, String rabbitmqPassword
            , String rabbitmqQueue) {
        this.rabbitHost = rabbitmqHost;
        this.rabbitPort = rabbitmqPort;
        this.rabbitUser = rabbitmqUser;
        this.rabbitPassword = rabbitmqPassword;
        this.tokenQueue = rabbitmqQueue;
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

    public void startListening() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setPort(rabbitPort);
        factory.setUsername(rabbitUser);
        factory.setPassword(rabbitPassword);

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(tokenQueue, true, false, false, null);

        System.out.println(" [*] Waiting for token request on queue '" + tokenQueue + "'. To exit press CTRL+C");

        DeliverCallback callback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            TokenDTO request = gson.fromJson(message, TokenDTO.class);
            TokenDTO response = new TokenDTO();
            try {
                switch (request.getType()) {
                    case "ADD_TOKENS":
                        tokenService.requestAddTokens(request.getCustomerID(), request.getAmount());
                        response.setType("SUCCESS");
                        break;

                    case "VALIDATE_TOKEN":
                        String customerID = tokenService.consumeToken(request.getToken());
                        if (customerID != null) {
                            response.setCustomerID(customerID);
                            response.setType("SUCCESS");
                            break;
                        }
                        response.setType("ERROR");
                        break;

                    default:
                        response.setType("ERROR");
                        response.setErrorMSG("Unknown request type: " + request.getType());
                }

            } catch (Exception e) {
                response.setType("ERROR");
                response.setErrorMSG(e.getMessage());
            }

            String replyTo = delivery.getProperties().getReplyTo();
            String correlationId = delivery.getProperties().getCorrelationId();
            if (replyTo != null) {
                sendResponse(channel, replyTo, correlationId, gson.toJson(response));
            }



        };

        channel.basicConsume(tokenQueue, true, callback, consumerTag -> { });
    }

    private void sendResponse(Channel channel, String replyTo, String correlationId, String response) throws IOException {
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .contentType("application/json")
                .deliveryMode(2)
                .build();
        channel.basicPublish("", replyTo, props, response.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws Exception {
        if (channel != null && channel.isOpen()) channel.close();
        if (connection != null && connection.isOpen()) connection.close();
    }
}
