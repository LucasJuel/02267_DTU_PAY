package org.g10.services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.g10.DTO.CustomerDTO;
import org.g10.DTO.TokenDTO;
import org.g10.utils.PublishWait;
/**
 @author ssschoubye
 **/

public class CustomerProducer implements AutoCloseable {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE = "account.customer";
    private static final String CUSTOMER_REPLY_QUEUE = "account.customer.reply";
    private static final String DEFAULT_QUEUE_CUSTOMER_DEREGISTER = "account.customer.deregister";
    private static final String TOKEN_QUEUE = "token.requests";
    private static final String DEFAULT_QUEUE_GET_CUSTOMER = "account.customer.get";

    private final Connection connection;
    private final Channel channel;
    private final String queueName;
    private final String customerDeregisterQueue;
    private final String getCustomerQueue;
    private TokenProducer tokenProducer;

    public CustomerProducer() throws IOException, TimeoutException {
        this(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                getEnv("RABBITMQ_QUEUE", DEFAULT_QUEUE),
                getEnv("RABBITMQ_QUEUE_CUSTOMER_DEREGISTER", DEFAULT_QUEUE_CUSTOMER_DEREGISTER),
                getEnv("RABBITMQ_QUEUE_GET_CUSTOMER", DEFAULT_QUEUE_GET_CUSTOMER)
        );
    }

    public CustomerProducer(String host, int port, String username, String password, String queueName, String customerDeregisterQueue, String getCustomerQueue)
            throws IOException, TimeoutException {
        this.queueName = queueName;
        this.customerDeregisterQueue = customerDeregisterQueue;
        this.getCustomerQueue = getCustomerQueue;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueDeclare(customerDeregisterQueue, true, false, false, null);
        channel.queueDeclare(getCustomerQueue, true, false, false, null);
 
    }

    public String publishCustomerRegistered(CustomerDTO customer) throws IOException, TimeoutException {
        tokenProducer = new TokenProducer(
            getEnv("RABBITMQ_HOST", DEFAULT_HOST),
            getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
            getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
            getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
            TOKEN_QUEUE
        );


        try{
           PublishWait publishWait = new PublishWait(
                    queueName,
                    CUSTOMER_REPLY_QUEUE,
                    channel,
                    customer
            );
            String returnedId = publishWait.getResponse();

            TokenDTO newCustomerToken = new TokenDTO();
            newCustomerToken.setCustomerID(returnedId);
            newCustomerToken.setType("ADD_TOKENS");
            newCustomerToken.setAmount(5);
            tokenProducer.sendTokenRequest(newCustomerToken);

            return returnedId;
        } catch(Exception e){
            e.printStackTrace();
            return "{ \"error\": \"Failed to publish message: " + e.getMessage() + "\" }";
        }
    }

    public String publishCustomerDeleted(String customerId) throws IOException {
        try{
            java.util.Map<String, String> message = java.util.Map.of("customerId", customerId);


           PublishWait publishWait = new PublishWait(
                    customerDeregisterQueue,
                    CUSTOMER_REPLY_QUEUE,
                    channel,
                    message
            );
            return publishWait.getResponse();
        } catch(Exception e){
            return "{ \"error\": \"Failed to publish message: " + e.getMessage() + "\" }";
        }
    }

    public CustomerDTO publishGetCustomer(String customerId) throws IOException {
        try{
            java.util.Map<String, String> message = java.util.Map.of("customerId", customerId);
              PublishWait publishWait = new PublishWait(
                    getCustomerQueue,
                    CUSTOMER_REPLY_QUEUE,
                    channel,
                    message
                );
            
            String response = publishWait.getResponse();
            com.google.gson.Gson gson = new com.google.gson.Gson();
            return gson.fromJson(response, CustomerDTO.class);
        } catch(Exception e){

            return null;
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
