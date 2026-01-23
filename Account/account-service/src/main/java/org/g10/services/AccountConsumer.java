package org.g10.services;

import com.rabbitmq.client.*;
import org.g10.DTO.CustomerDTO;
import org.g10.DTO.MerchantDTO;
import com.rabbitmq.client.ConnectionFactory;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 @author gh05tdog
 **/
public class AccountConsumer implements AutoCloseable {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE_MERCHANT = "account.merchant";
    private static final String DEFAULT_QUEUE_CUSTOMER = "account.customer";
    private static final String DEFAULT_QUEUE_CUSTOMER_DEREGISTER = "account.customer.deregister";
    private static final String DEFAULT_QUEUE_MERCHANT_DEREGISTER = "account.merchant.deregister";
    private static final String DEFAULT_QUEUE_REPORTING = "reporting.requests";

    private final String rabbitHost;
    private final int rabbitPort;
    private final String rabbitUser;
    private final String rabbitPassword;
    private final String customerQueue;
    private final String merchantQueue;
    private final String customerDeregisterQueue;
    private final String merchantDeregisterQueue;
    private final String reportingQueue;

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
                getEnv("RABBITMQ_QUEUE_CUSTOMER", DEFAULT_QUEUE_CUSTOMER),
                getEnv("RABBITMQ_QUEUE_CUSTOMER_DEREGISTER", DEFAULT_QUEUE_CUSTOMER_DEREGISTER),
                getEnv("RABBITMQ_QUEUE_MERCHANT_DEREGISTER", DEFAULT_QUEUE_MERCHANT_DEREGISTER),
                getEnv("RABBITMQ_QUEUE_REPORTING", DEFAULT_QUEUE_REPORTING)
        );
    }

    public AccountConsumer(String rabbitmqHost, int rabbitmqPort, String rabbitmqUser, String rabbitmqPassword, String rabbitmqQueue
            , String rabbitmqQueueCustomer, String customerDeregisterQueue, String merchantDeregisterQueue, String rabbitmqQueueReporting) {  
        this.rabbitHost = rabbitmqHost;
        this.rabbitPort = rabbitmqPort;
        this.rabbitUser = rabbitmqUser;
        this.rabbitPassword = rabbitmqPassword;
        this.merchantQueue = rabbitmqQueue;
        this.customerQueue = rabbitmqQueueCustomer;
        this.customerDeregisterQueue = customerDeregisterQueue;
        this.merchantDeregisterQueue = merchantDeregisterQueue;
        this.reportingQueue = rabbitmqQueueReporting;
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
        channel.queueDeclare(customerDeregisterQueue, true, false, false, null);
        channel.queueDeclare(merchantDeregisterQueue, true, false, false, null);
        channel.queueDeclare(reportingQueue, true, false, false, null);

        System.out.println(" [*] Waiting for messages To exit press CTRL+C");
        DeliverCallback customerCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [CUSTOMER] Received '" + message + "'");
            try {
                CustomerDTO customer = gson.fromJson(message, CustomerDTO.class);
                String response = customerService.register(customer);
                String replyTo = delivery.getProperties().getReplyTo();
                if (replyTo != null && !replyTo.isBlank()) {
                    String correlationId = delivery.getProperties().getCorrelationId();
                    System.out.println(" [CUSTOMER] Sending response:" + response + " to " + replyTo + " with correlationId " + correlationId);
                    sendResponse(channel, replyTo, correlationId, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        DeliverCallback merchantCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [MERCHANT] Received '" + message + "'");
            try {
                MerchantDTO merchant = gson.fromJson(message, MerchantDTO.class);
                String response = merchantService.register(merchant);
                String replyTo = delivery.getProperties().getReplyTo();
                if (replyTo != null && !replyTo.isBlank()) {
                    String correlationId = delivery.getProperties().getCorrelationId();
                    System.out.println(" [MERCHANT] Sending response:" + response + " to " + replyTo + " with correlationId " + correlationId);
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

        DeliverCallback customerDeregisterCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received deregister request for customer: '" + message + "'");
            try {
                // Parse JSON to extract customerId
                com.google.gson.JsonObject jsonObject = gson.fromJson(message, com.google.gson.JsonObject.class);
                String customerId = jsonObject.get("customerId").getAsString();
                System.out.println("Deregister customer with ID: " + customerId);
                String response = customerService.deregister(customerId);
                System.out.println("Deregister customer response: " + response);
                String replyTo = delivery.getProperties().getReplyTo();
                if (replyTo != null && !replyTo.isBlank()) {
                    String correlationId = delivery.getProperties().getCorrelationId();
                    System.out.println("Sending deregister response: " + response + " to " + replyTo);
                    sendResponse(channel, replyTo, correlationId, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        DeliverCallback merchantDeregisterCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received deregister request for merchant: '" + message + "'");
            try {
                // Parse JSON to extract merchantId
                com.google.gson.JsonObject jsonObject = gson.fromJson(message, com.google.gson.JsonObject.class);
                String merchantId = jsonObject.get("merchantId").getAsString();
                System.out.println("Deregister merchant with ID: " + merchantId);
                String response = merchantService.deregister(merchantId);
                System.out.println("Deregister merchant response: " + response);
                String replyTo = delivery.getProperties().getReplyTo();
                if (replyTo != null && !replyTo.isBlank()) {
                    String correlationId = delivery.getProperties().getCorrelationId();
                    System.out.println("Sending deregister response: " + response + " to " + replyTo);
                    sendResponse(channel, replyTo, correlationId, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        channel.basicConsume(customerDeregisterQueue, true, customerDeregisterCallback, consumerTag -> {
        });
        channel.basicConsume(merchantDeregisterQueue, true, merchantDeregisterCallback, consumerTag -> {
        });

    }

    public CustomerService getCustomerService() {
        return customerService;
    }

    public MerchantService getMerchantService() {
        return merchantService;
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
