package org.g10.services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.g10.DTO.MerchantDTO;
import org.g10.utils.PublishWait;


public class MerchantProducer implements AutoCloseable {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE = "merchant.events";
    private static final String MERCHANT_REPLY_QUEUE = "account.merchant.reply";
    private static final String DEFAULT_QUEUE_MERCHANT_DEREGISTER = "account.merchant.deregister";

    private final Connection connection;
    private final Channel channel;
    private final String queueName;
    private final String merchantDeregisterQueue;

    public MerchantProducer() throws IOException, TimeoutException {
        this(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt(),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                getEnv("RABBITMQ_MERCHANT_QUEUE", DEFAULT_QUEUE),
                getEnv("RABBITMQ_QUEUE_MERCHANT_DEREGISTER", DEFAULT_QUEUE_MERCHANT_DEREGISTER)
        );
    }

    public MerchantProducer(String host, int port, String username, String password, String queueName, String merchantDeregisterQueue)
            throws IOException, TimeoutException {
        this.queueName = queueName;
        this.merchantDeregisterQueue = merchantDeregisterQueue;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueDeclare(merchantDeregisterQueue, true, false, false, null);
    }

    public String publishMerchantRegistered(MerchantDTO merchant) throws IOException {
        try{
            PublishWait publishWait = new PublishWait(
                    queueName,
                    MERCHANT_REPLY_QUEUE,
                    channel,
                    merchant
            );
            return publishWait.getResponse();
        } catch (Exception e){
            e.printStackTrace();
            return "{ \"error\": \"Failed to publish message: " + e.getMessage() + "\" }";
        }
    }

    public String publishMerchantDeleted(String merchantId) throws IOException {
        try{
            java.util.Map<String, String> message = java.util.Map.of("merchantId", merchantId);

            PublishWait publishWait = new PublishWait(
                    merchantDeregisterQueue,
                    MERCHANT_REPLY_QUEUE,
                    channel,
                    message
            );
            return publishWait.getResponse();
        } catch(Exception e){
            return "{ \"error\": \"Failed to publish message: " + e.getMessage() + "\" }";
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
            return MerchantProducer.DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return MerchantProducer.DEFAULT_PORT;
        }
    }
}
