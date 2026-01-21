package org.g10.services;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.g10.DTO.CustomerDTO;

public class CustomerProducer implements AutoCloseable {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE = "account.customer";

    private final Connection connection;
    private final Channel channel;
    private final String queueName;

    public CustomerProducer() throws IOException, TimeoutException {
        this(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                getEnv("RABBITMQ_QUEUE", DEFAULT_QUEUE)
        );
    }

    public CustomerProducer(String host, int port, String username, String password, String queueName)
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

    public void publishCustomerRegistered(CustomerDTO customer) throws IOException {
        String payload = toJson(customer);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .deliveryMode(2)
                .build();
        channel.basicPublish("", queueName, props, payload.getBytes(StandardCharsets.UTF_8));
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

    private static String toJson(CustomerDTO customer) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        addOrNull(builder, "firstName", customer.getFirstName());
        addOrNull(builder, "lastName", customer.getLastName());
        addOrNull(builder, "cpr", customer.getCpr());
        addOrNull(builder, "bankAccountId", customer.getBankAccountId());
        return builder.build().toString();
    }

    private static void addOrNull(JsonObjectBuilder builder, String key, String value) {
        if (value == null) {
            builder.addNull(key);
        } else {
            builder.add(key, value);
        }
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
}
