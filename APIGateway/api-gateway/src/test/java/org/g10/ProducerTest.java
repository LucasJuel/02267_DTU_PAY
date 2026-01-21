package org.g10;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.cucumber.java.Before;
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
import org.g10.DTO.MerchantDTO;
import org.g10.DTO.PaymentDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProducerTest {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String CUSTOMER_QUEUE = "account.customer";
    private static final String MERCHANT_QUEUE = "account.merchant";
    private static final String PAYMENT_QUEUE = "payment.requests";
    private static final String QUEUE_NAME = "rabbit.test";

    private Connection connection;
    private Channel channel;
    private CustomerProducer customerProducer;
    private MerchantProducer merchantProducer;
    private PaymentProducer paymentProducer;
    private CustomerDTO customer;
    private MerchantDTO merchant;
    private PaymentDTO payment;

    @Before
    public void setUp() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(getEnv("RABBITMQ_HOST", DEFAULT_HOST));
        factory.setPort(getEnvInt("RABBITMQ_PORT", DEFAULT_PORT));
        factory.setUsername(getEnv("RABBITMQ_USER", DEFAULT_USERNAME));
        factory.setPassword(getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD));
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(CUSTOMER_QUEUE, true, false, false, null);
        channel.queueDeclare(MERCHANT_QUEUE, true, false, false, null);
        channel.queueDeclare(PAYMENT_QUEUE, true, false, false, null);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);


        customerProducer = new CustomerProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                CUSTOMER_QUEUE
        );
        merchantProducer = new MerchantProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                MERCHANT_QUEUE
        );

        paymentProducer = new PaymentProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                PAYMENT_QUEUE
        );
    }


    @Given("a RabbitMQ connection")
    public void a_rabbit_mq_connection() {
        assertNotNull(connection);
        assertNotNull(channel);
    }

    @Given("a customer with first name {string}, last name {string} and cpr {string}")
    public void a_customer_with_first_name_last_name_and_cpr(String string, String string2, String string3) {     
        customer = new CustomerDTO(string, string2, string3, "");
    }

    @Given("the customer have a bank account with the bank account id {string}")
    public void the_customer_have_a_bank_account_with_a_balance_of_dkk(String string) {
        customer.setBankAccountId(string);
    }

    @When("I make a request to register the customer in DTU Pay")
    public void i_make_a_request_to_register_the_customer_in_dtu_pay() throws Exception {
        customerProducer.publishCustomerRegistered(customer);
    }

    @Then("the customer is registered successfully")
    public void the_customer_is_registered_successfully() throws Exception {
        String payload = readMessage(CUSTOMER_QUEUE);
        JsonObject json = Json.createReader(new StringReader(payload)).readObject();
        assertEquals(customer.getFirstName(), json.getString("firstName"));
        assertEquals(customer.getLastName(), json.getString("lastName"));
        assertEquals(customer.getCpr(), json.getString("cpr"));
        assertEquals(customer.getBankAccountId(), json.getString("bankAccountId"));
    }


    @Given("a merchant with first name {string} and last name {string} and cpr {string}")
    public void a_merchant_with_first_name_and_last_name_and_cpr(String string, String string2, String string3) {
        merchant = new MerchantDTO(string, string2, string3, "");
    }

    @Given("the merchant have a bank account with the bank account id {string}")
    public void the_merchant_have_a_bank_account_with_the_bank_account_id(String string) {
        merchant.setBankAccountId(string);
    }

    @When("I make a request to register the merchant in DTU Pay")
    public void i_make_a_request_to_register_the_merchant_in_dtu_pay() {
        try{
            merchantProducer.publishMerchantRegistered(merchant);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Then("the merchant is registered successfully")
    public void the_merchant_is_registered_successfully() {
        try{
            String payload = readMessage(MERCHANT_QUEUE);
            JsonObject json = Json.createReader(new StringReader(payload)).readObject();
            assertEquals(merchant.getFirstName(), json.getString("firstName"));
            assertEquals(merchant.getLastName(), json.getString("lastName"));
            assertEquals(merchant.getCpr(), json.getString("cpr"));
            assertEquals(merchant.getBankAccountId(), json.getString("bankAccountId"));
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        
    }

    @Given("a payment with amount {int} is initiated by merchant with id {string} from customer with id {string}")
    public void a_payment_with_amount_is_initiated_by_merchant_with_id_from_customer_with_id(Integer int1, String string, String string2) {       
        payment = new PaymentDTO(string2, string, int1.floatValue(), "Test payment");
    }

    @When("I make a request to initiate the payment in DTU Pay")
    public void i_make_a_request_to_initiate_the_payment_in_dtu_pay() {
        try{
            paymentProducer.publishPaymentRequested(payment);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Then("the payment is initiated successfully")
    public void the_payment_is_initiated_successfully() {
        try{
            String payload = readMessage(PAYMENT_QUEUE);
            JsonObject json = Json.createReader(new StringReader(payload)).readObject();
            assertEquals(payment.getCustomerAccountId(), json.getString("customerAccountId"));
            assertEquals(payment.getMerchantAccountId(), json.getString("merchantAccountId"));
            assertEquals(payment.getAmount(), (float) json.getJsonNumber("amount").doubleValue(), 0.001f);
            assertEquals(payment.getMessage(), json.getString("message"));
        } catch (Exception e){
            throw new RuntimeException(e);
        }
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
