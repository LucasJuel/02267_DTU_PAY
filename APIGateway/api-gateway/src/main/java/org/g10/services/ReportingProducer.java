package org.g10.services;

import org.g10.DTO.ReportDTO;
import org.g10.utils.PublishWait;
import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class ReportingProducer implements AutoCloseable {
    private static final String DEFAULT_HOST = "rabbitmq";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE = "reporting.requests";
    private static final String REPORT_REPLY_QUEUE = "reporting.reply";

    private final Connection connection;
    private final Channel channel;
    private final String queueName;

    public ReportingProducer() throws IOException, TimeoutException {
        this(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                getEnv("RABBITMQ_REPORTING_QUEUE", DEFAULT_QUEUE)
        );
    }

    public ReportingProducer(String host, int port, String username, String password, String queueName)
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

    public String publishReportRequest(ReportDTO report) throws IOException {
        try{
            PublishWait publishWait = new PublishWait(
                this.queueName,
                REPORT_REPLY_QUEUE,
                this.channel,
                report
            );
            return publishWait.getResponse();
        } catch (Exception e){
            e.printStackTrace();
            return "{ \"error\": \"Failed to publish message: " + e.getMessage() + "\" }";
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
