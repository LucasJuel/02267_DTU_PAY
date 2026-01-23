package org.g10.services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.OAuth2ClientCredentialsGrantCredentialsProvider.Token;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.g10.DTO.PaymentDTO;
import org.g10.DTO.TokenDTO;
import org.g10.utils.PublishWait;
/**
 @author LucasJuel
 **/


public class PaymentProducer implements AutoCloseable {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String DEFAULT_QUEUE = "payment.events";
    private static final String PAYMENT_REPLY_QUEUE = "payment.reply";
    private static final String TOKEN_QUEUE = "token.requests";
    private static final String TOKEN_REPLY_QUEUE = "token.reply";
    private static final String CUSTOMER_QUEUE = "account.customer";
    private static final String CUSTOMER_REPLY_QUEUE = "account.customer.reply";
    private static final String GET_CUSTOMER_QUEUE = "account.customer.get";
    private static final String CUSTOMER_DEREGISTER_QUEUE = "account.customer.deregister";

    private final Connection connection;
    private final Channel channel;
    private final String queueName;
    private String bankAccountId;
    private TokenProducer tokenProducer;
    private CustomerProducer customerProducer;

    public PaymentProducer() throws IOException, TimeoutException {
        this(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                getEnv("RABBITMQ_PAYMENT_QUEUE", DEFAULT_QUEUE)
        );
    }

    

    public PaymentProducer(String host, int port, String username, String password, String queueName)
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



    public String publishPaymentRequested(PaymentDTO payment) throws IOException, TimeoutException {
        tokenProducer = new TokenProducer(
            getEnv("RABBITMQ_HOST", DEFAULT_HOST),
            getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
            getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
            getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
            TOKEN_QUEUE

        );
        
        customerProducer = new CustomerProducer(
            getEnv("RABBITMQ_HOST", DEFAULT_HOST),
            getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
            getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
            getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
            getEnv("RABBITMQ_QUEUE", CUSTOMER_QUEUE),
            getEnv("RABBITMQ_QUEUE_CUSTOMER_DEREGISTER", CUSTOMER_DEREGISTER_QUEUE),
            getEnv("RABBITMQ_QUEUE_GET_CUSTOMER", GET_CUSTOMER_QUEUE)
        );


        TokenDTO tokenRequest = new TokenDTO();
        tokenRequest.setToken(payment.getCustomerToken());
        tokenRequest.setType("VALIDATE_TOKEN");

        String customerId;
        try{
            customerId = tokenProducer.sendTokenRequest(tokenRequest).getCustomerID();

            bankAccountId = customerProducer.publishGetCustomer(customerId).getBankAccountId();

        } catch (Exception e){
            return "{\"error\": \"Failed to process payment: Account not found\" }";
        }




        payment.setCustomerAccountId(bankAccountId);



        try{
            PublishWait publishWait = new PublishWait(
                    queueName,
                    PAYMENT_REPLY_QUEUE,
                    channel,
                    payment
            );
            return publishWait.getResponse();
        } catch (Exception e){
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
