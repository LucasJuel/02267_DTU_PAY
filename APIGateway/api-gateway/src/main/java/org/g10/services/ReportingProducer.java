package org.g10.services;

import org.g10.DTO.ReportRequestDTO;

import com.rabbitmq.client.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;


public class ReportingProducer implements AutoCloseable {
    private static final String DEFAULT_HOST = "rabbitmq";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE = "reporting.requests";

    private final Connection connection;
    private final Channel channel;
    private final String queueName;
    private final Gson gson = new Gson();

    public ReportingProducer() throws IOException, TimeoutException {
        this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_QUEUE);
    }

    public ReportingProducer(String host, int port, String username, String password, String queueName)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.queueName = queueName;
        channel.queueDeclare(queueName, true, false, false, null);
    }

    public void publishReportRequest(String customerId) throws IOException {
        String payload = gson.toJson(new ReportRequestDTO(customerId));
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
}
