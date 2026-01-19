package org.g10;
import io.cucumber.java.PendingException;
import org.g10.DTO.PaymentDTO;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.g10.services.PaymentConsumer;
import org.g10.services.PaymentServiceApplication;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PaymentConsumerSteps {
    private PaymentConsumer consumer;
    private PaymentDTO payment;
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private PaymentServiceApplication app;
    private String payment_result;
    private Thread thread;

    @Before
    public void setup() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare("payment.requests", true, false, false, null);
        
        thread = new Thread(() -> {
            app = new PaymentServiceApplication();
            PaymentServiceApplication.main(new String[]{});
        });
        thread.start();
        try {
            Thread.sleep(2000); // Wait for the service to start
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Given("the payment service is running")
    public void a_fresh_payment_store() {
        // Storage already reset in @Before
        assertNotNull(app);

    }

    @And("a transaction exists with the following payload:")
    public void a_transaction_exists_with_the_following_payload(DataTable table) {
        Map<String, String> details = table.asMap(String.class, String.class);


        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setAmount(new BigDecimal(details.get("amount")));
        paymentDTO.setCustomerAccountId(details.get("customerId"));
        paymentDTO.setMerchantAccountId(details.get("merchantId"));
        paymentDTO.setMessage(details.get("message"));

    }

    @When("I register the payment with the payment service") 
    public void i_register_the_payment_with_the_payment_service() {
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            String replyQueue = channel.queueDeclare("", false, true, true, null).getQueue();
            CompletableFuture <String> future = new CompletableFuture<>();
            String consumerTag = channel.basicConsume(replyQueue, true, (tag, message) -> {
                if (correlationId.equals(message.getProperties().getCorrelationId())) {
                    future.complete(new String(message.getBody()));
                }
                }, tag -> {
                });
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueue)
                    .build();
            String payload = new Gson().toJson(payment);
            channel.basicPublish("", "payment.requests", props, payload.getBytes());
            payment_result = future.get(5, java.util.concurrent.TimeUnit.SECONDS); // Wait for the response
            System.out.println("Received response: " + payment_result);
            channel.basicCancel(consumerTag);

        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Then("the payment service should respond with a success message")
    public void the_payment_service_should_respond_with_a_success_message() {
        assertNotNull(payment_result);
        System.out.println("Payment service response: " + payment_result);
        // Further assertions can be made based on the expected response format
    }

    @And("^a transaction request comes through rabbitMQ$")
    public void aTransactionRequestComesThroughRabbitMQ() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
