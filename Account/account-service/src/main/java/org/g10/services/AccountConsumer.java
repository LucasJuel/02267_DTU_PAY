package org.g10.services;

import com.rabbitmq.client.*;
import org.g10.DTO.CustomerDTO;
import org.g10.DTO.MerchantDTO;
import org.g10.services.CustomerService;
import org.g10.services.MerchantService;
import com.rabbitmq.client.ConnectionFactory;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;


public class AccountConsumer implements AutoCloseable {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE_MERCHANT = "account.merchant";
    private static final String DEFAULT_QUEUE_CUSTOMER = "account.customer";

    private final String rabbitHost;
    private final int rabbitPort;
    private final String rabbitUser;
    private final String rabbitPassword;
    private final String customerQueue;
    private final String merchantQueue;

    private final Gson gson = new Gson();
    private final CustomerService customerService = new CustomerService();
    private final MerchantService merchantService = new MerchantService();


    public AccountConsumer() throws IOException, TimeoutException {
        this(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                getEnv("RABBITMQ_QUEUE_MERCHANT", DEFAULT_QUEUE_MERCHANT),
                getEnv("RABBITMQ_QUEUE_CUSTOMER", DEFAULT_QUEUE_CUSTOMER)
        );
    }

    public AccountConsumer(String rabbitmqHost, int rabbitmqPort, String rabbitmqUser, String rabbitmqPassword, String rabbitmqQueue
            , String rabbitmqQueueCustomer) {
        this.rabbitHost = rabbitmqHost;
        this.rabbitPort = rabbitmqPort;
        this.rabbitUser = rabbitmqUser;
        this.rabbitPassword = rabbitmqPassword;
        this.merchantQueue = rabbitmqQueue;
        this.customerQueue = rabbitmqQueueCustomer;
    }

    public void startListening() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setPort(rabbitPort);
        factory.setUsername(rabbitUser);
        factory.setPassword(rabbitPassword);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(customerQueue, true, false, false, null);
        channel.queueDeclare(merchantQueue, true, false, false, null);

        System.out.println(" [*] Waiting for messages To exit press CTRL+C");
        DeliverCallback customerCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            try {
                CustomerDTO customer = gson.fromJson(message, CustomerDTO.class);
                String response = customerService.register(customer);
                String replyTo = delivery.getProperties().getReplyTo();
                if (replyTo != null && !replyTo.isBlank()) {
                    String correlationId = delivery.getProperties().getCorrelationId();
                    sendResponse(channel, replyTo, correlationId, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        DeliverCallback merchantCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            try {
                MerchantDTO merchant = gson.fromJson(message, MerchantDTO.class);
                String response = merchantService.register(merchant);
                String replyTo = delivery.getProperties().getReplyTo();
                if (replyTo != null && !replyTo.isBlank()) {
                    String correlationId = delivery.getProperties().getCorrelationId();
                    sendResponse(channel, replyTo, correlationId, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        channel.basicConsume(customerQueue, true, customerCallback, consumerTag -> {
        });
        channel.basicConsume(merchantQueue, true, merchantCallback, consumerTag -> {
        });

    }

    public CustomerService getCustomerService() {
        return customerService;
    }

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

    }
}
