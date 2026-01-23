package org.g10.services;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.g10.DTO.TokenDTO;
import org.g10.utils.PublishWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/**
 @author Martin-Surlykke
 **/
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
        try {
       
            PublishWait publishWait = new PublishWait(
                    this.queueName,
                    "token.reply",
                    this.channel,
                    tokenDTO
            );

            String jsonResponse = publishWait.getResponse();
          
            return new Gson().fromJson(jsonResponse, TokenDTO.class);
        } catch (Exception e) {
            TokenDTO errorResponse = new TokenDTO();
            errorResponse.setType("ERROR");
            errorResponse.setErrorMSG("Request failed or timed out: " + e.getMessage());
            return errorResponse;
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