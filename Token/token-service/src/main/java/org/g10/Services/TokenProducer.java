package org.g10.Services;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.g10.DTO.TokenDTO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

public class TokenProducer implements AutoCloseable {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE = "token.requests";

    private final Connection connection;
    private final Channel channel;
    private final String queueName;

    public TokenProducer() throws IOException, TimeoutException {
        this(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt(),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                getEnv("RABBITMQ_TOKEN_QUEUE", DEFAULT_QUEUE)
        );
    }

    public TokenProducer(String host, int port, String username, String password, String queueName)
            throws IOException, TimeoutException {
        this.queueName = queueName;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
    }

    public TokenDTO sendTokenRequest(TokenDTO tokenDTO) throws IOException, InterruptedException {
        Gson gson = new Gson();
        String payload = gson.toJson(tokenDTO);
        String temporaryQueue = channel.queueDeclare().getQueue();
        String correlationID = java.util.UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(correlationID)
                .replyTo(temporaryQueue)
                .contentType("application/json")
                .deliveryMode(2)
                .build();

        channel.basicPublish("", queueName, props, payload.getBytes(StandardCharsets.UTF_8));
        final BlockingQueue<String> responseQueue = new LinkedBlockingDeque<>(1);
        // Found with CHATGPT, we need the queue to wait for result

        String cTag = channel.basicConsume(temporaryQueue, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(correlationID)) {
                responseQueue.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        }, consumerTag -> { });

        String result = responseQueue.take();
        channel.basicCancel(cTag);
        return gson.fromJson(result, TokenDTO.class);
    }

    @Override
    public void close() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }

    private static String getEnv(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int getEnvInt() {
        String value = System.getenv("RABBITMQ_PORT");
        if (value == null || value.isBlank()) {
            return TokenProducer.DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return TokenProducer.DEFAULT_PORT;
        }
    }
}