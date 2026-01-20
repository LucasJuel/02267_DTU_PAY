package org.g10;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.g10.DTO.CustomerDTO;
import org.g10.services.CustomerProducer;
import org.g10.services.MerchantProducer;
import org.g10.services.PaymentProducer;
import org.g10.services.ReportingProducer;
import org.g10.DTO.MerchantDTO;
import org.g10.DTO.PaymentDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProducerTest {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String CUSTOMER_QUEUE = "customer.events";
    private static final String MERCHANT_QUEUE = "merchant.events";
    private static final String PAYMENT_QUEUE = "payment.events";
    private static final String REPORTING_QUEUE = "reporting.requests";
    private static final String QUEUE_NAME = "rabbit.test";

    private Connection connection;
    private Channel channel;

    @Given("a RabbitMQ connection")
    public void aRabbitMqConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(getEnv("RABBITMQ_HOST", DEFAULT_HOST));
        factory.setPort(getEnvInt());
        factory.setUsername(getEnv("RABBITMQ_USER", DEFAULT_USERNAME));
        factory.setPassword(getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD));
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(CUSTOMER_QUEUE, true, false, false, null);
        channel.queueDeclare(MERCHANT_QUEUE, true, false, false, null);
        channel.queueDeclare(PAYMENT_QUEUE, true, false, false, null);
        channel.queueDeclare(REPORTING_QUEUE, true, false, false, null);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
    }

    @When("I publish a customer registration event")
    public void iPublishACustomerRegistrationEvent() throws Exception {
        CustomerDTO customer = new CustomerDTO("Ada", "Lovelace", "111111-1111", "cust-account-1");
        try (CustomerProducer producer = new CustomerProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt(),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                CUSTOMER_QUEUE
        )) {
            producer.publishCustomerRegistered(customer);
        }
    }

    @Then("the customer event is available on the customer queue")
    public void theCustomerEventIsAvailableOnTheCustomerQueue() throws Exception {
        String payload = readMessage(CUSTOMER_QUEUE);
        JsonObject json = Json.createReader(new StringReader(payload)).readObject();
        assertEquals("Ada", json.getString("firstName"));
        assertEquals("Lovelace", json.getString("lastName"));
        assertEquals("111111-1111", json.getString("cpr"));
        assertEquals("cust-account-1", json.getString("bankAccountId"));
    }

    @When("I publish a merchant registration event")
    public void iPublishAMerchantRegistrationEvent() throws Exception {
        MerchantDTO merchant = new MerchantDTO("Grace", "Hopper", "222222-2222", "merchant-account-1");
        try (MerchantProducer producer = new MerchantProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt(),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                MERCHANT_QUEUE
        )) {
            producer.publishMerchantRegistered(merchant);
        }
    }

    @Then("the merchant event is available on the merchant queue")
    public void theMerchantEventIsAvailableOnTheMerchantQueue() throws Exception {
        String payload = readMessage(MERCHANT_QUEUE);
        JsonObject json = Json.createReader(new StringReader(payload)).readObject();
        assertEquals("Grace", json.getString("firstName"));
        assertEquals("Hopper", json.getString("lastName"));
        assertEquals("222222-2222", json.getString("cpr"));
        assertEquals("merchant-account-1", json.getString("bankAccountId"));
    }

    @When("I publish a payment request event")
    public void iPublishAPaymentRequestEvent() throws Exception {
        PaymentDTO payment = new PaymentDTO("cust-1", "merchant-1", 12.50f, "test payment");
        try (PaymentProducer producer = new PaymentProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt(),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                PAYMENT_QUEUE
        )) {
            producer.publishPaymentRequested(payment);
        }
    }

    @Then("the payment event is available on the payment queue")
    public void thePaymentEventIsAvailableOnThePaymentQueue() throws Exception {
        String payload = readMessage(PAYMENT_QUEUE);
        JsonObject json = Json.createReader(new StringReader(payload)).readObject();
        assertEquals("cust-1", json.getString("customerAccountId"));
        assertEquals("merchant-1", json.getString("merchantAccountId"));
        assertEquals(12.50f, (float) json.getJsonNumber("amount").doubleValue(), 0.001f);
        assertEquals("test payment", json.getString("message"));
    }

    @When("I publish {string} to the rabbit test queue")
    public void iPublishToTheRabbitTestQueue(String message) throws Exception {
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Then("I can consume {string} from the rabbit test queue")
    public void iCanConsumeFromTheRabbitTestQueue(String expected) throws Exception {
        var delivery = channel.basicGet(QUEUE_NAME, true);
        assertNotNull(delivery, "Expected a message to be available on the test queue.");
        String actual = new String(delivery.getBody(), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(expected, actual);
    }

    @When("I request a report for customer with ID {string}")
    public void i_request_a_report_for_customer_with_id(String string) {
        try (ReportingProducer producer = new ReportingProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt(),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                REPORTING_QUEUE
        )) {
            producer.publishReportRequest(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Then("a report request event is available on the reporting queue")
    public void a_report_request_event_is_available_on_the_reporting_queue() throws Exception{
        String payload = readMessage(REPORTING_QUEUE);
        JsonObject json = Json.createReader(new StringReader(payload)).readObject();
        assertEquals("customer-123", json.getString("customerId"));
    }


    @After
    public void cleanup() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }

    private String readMessage(String queueName) throws Exception {
        var delivery = channel.basicGet(queueName, true);
        assertNotNull(delivery, "Expected a message to be available on the queue.");
        return new String(delivery.getBody(), StandardCharsets.UTF_8);
    }

    private static String getEnv(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int getEnvInt() {
        String value = System.getenv("RABBITMQ_PORT");
        if (value == null || value.isBlank()) {
            return ProducerTest.DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return ProducerTest.DEFAULT_PORT;
        }
    }
}
