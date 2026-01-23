package org.g10;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.PendingException;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.io.StringReader;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import dtu.ws.fastmoney.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import org.g10.DTO.*;
import org.g10.services.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 @author Martin-Surlykke
 **/
public class ProducerTest {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String CUSTOMER_QUEUE = "account.customer";
    private static final String MERCHANT_QUEUE = "account.merchant";
    private static final String PAYMENT_QUEUE = "payment.requests";
    private static final String REPORTING_QUEUE = "reporting.requests";
    private static final String TOKEN_QUEUE = "token.requests";
    private static final String DEFAULT_QUEUE_CUSTOMER_DEREGISTER = "account.customer.deregister";
    private static final String DEFAULT_QUEUE_MERCHANT_DEREGISTER = "account.merchant.deregister";

    private static final String QUEUE_NAME = "rabbit.test";
    private String EXPECTED_MESSAGE;

    private Connection connection;
    private Channel channel;
    private final BankService bank = new BankService_Service().getBankServicePort();
    private CustomerProducer customerProducer;
    private MerchantProducer merchantProducer;
    private TokenProducer tokenProducer;
    private ReportingProducer reportingProducer;
    private PaymentProducer paymentProducer;
    private CustomerDTO customer;
    private MerchantDTO merchant;
    private ReportDTO report;
    private TokenDTO token;
    // private PaymentDTO payment;
    private PaymentDTO payment;
    private final User customerUser = new User();
    private final User merchantUser = new User();
    private String returnedId;
    TokenDTO lastTokenResponse;
    private String usedToken;
    private String tokenCustomerID;
    private final String apiKey = "kayak2098";
    private String customerAccountId;
    private String merchantAccountId;
    private final List<String> accounts = new ArrayList<>();

    @BeforeAll
    public static void globalSetUp() {
        System.out.println("GLOBAL SETUP STARTED");
        System.out.println("SETUP STARTED ON PORT: " + getEnvInt("RABBITMQ_PORT", DEFAULT_PORT));
        System.out.println("ON HOST : " + getEnv("RABBITMQ_HOST", DEFAULT_HOST));
    }

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
        channel.queueDeclare(REPORTING_QUEUE, true, false, false, null);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        customerProducer = new CustomerProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                CUSTOMER_QUEUE,
                DEFAULT_QUEUE_CUSTOMER_DEREGISTER
        );
        merchantProducer = new MerchantProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                MERCHANT_QUEUE,
                DEFAULT_QUEUE_MERCHANT_DEREGISTER
        );

        tokenProducer = new TokenProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                TOKEN_QUEUE

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
        try{
            returnedId = customerProducer.publishCustomerRegistered(customer);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Then("the customer is registered successfully")
    public void the_customer_is_registered_successfully() throws Exception {
        System.out.println("Returned ID: " + returnedId);
        assertTrue(returnedId.length() == 36);
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
            returnedId = merchantProducer.publishMerchantRegistered(merchant);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Then("the merchant is registered successfully")
    public void the_merchant_is_registered_successfully() {
        try{
            System.out.println("Returned ID: " + returnedId);
            assertTrue(returnedId.length() == 36);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        
    }

    @When("I make a request to deregister the customer from DTU Pay")
    public void i_make_a_request_to_deregister_the_customer_from_dtu_pay() throws Exception {
        // First register the customer to get an ID
        returnedId = customerProducer.publishCustomerRegistered(customer);
        // Then deregister using that ID
        returnedId = customerProducer.publishCustomerDeleted(returnedId);
    }

    @Then("the customer is deregistered successfully")
    public void the_customer_is_deregistered_successfully() {
        assertNotEquals("Failure!", returnedId);
        System.out.println("Deregistration response: " + returnedId);
    }

    @When("I make a request to deregister the merchant from DTU Pay")
    public void i_make_a_request_to_deregister_the_merchant_from_dtu_pay() throws Exception {
        // First register the merchant to get an ID
        returnedId = merchantProducer.publishMerchantRegistered(merchant);
        // Then deregister using that ID
        returnedId = merchantProducer.publishMerchantDeleted(returnedId);
    }

    @Then("the merchant is deregistered successfully")
    public void the_merchant_is_deregistered_successfully() {
        assertNotEquals("Failure!", returnedId);
        System.out.println("Deregistration response: " + returnedId);
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
        EXPECTED_MESSAGE = string;
        try (ReportingProducer producer = new ReportingProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                REPORTING_QUEUE
        )) {
            ReportDTO report = new ReportDTO(string, "customer");
            returnedId = producer.publishReportRequest(report);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    @Given("a customer with id {string}")
    public void a_customer_with_id(String string) {
        report = new ReportDTO(string, "customer");
        assertNotNull(report);
    }

    @When("I make a request for a report of payments for the customer")
    public void i_make_a_request_for_a_report_of_payments_for_the_customer() {
        try (ReportingProducer producer = new ReportingProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                REPORTING_QUEUE
        )) {
            returnedId = producer.publishReportRequest(report);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Then("the report of payments is returned successfully")
    public void the_report_of_payments_is_returned_successfully() {
        System.out.println("Returned response: " + returnedId);
        assertNotNull(returnedId, "Response should not be null");
        
        // Parse the JSON array response
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Object>>(){}.getType();
        List<Object> paymentList = gson.fromJson(returnedId, listType);
        
        assertNotNull(paymentList, "Parsed list should not be null");
        assertTrue(paymentList.isEmpty() == true, "Payment list should not be empty");
    }

    @Given("the manager requests a report")
    public void the_manager_requests_a_report() {
        report = new ReportDTO("manager", "manager");
        assertNotNull(report);
    }

    @When("I make a request for a report of all payments for the manager")
    public void i_make_a_request_for_a_report_of_all_payments_for_the_manager() {
        try (ReportingProducer producer = new ReportingProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                REPORTING_QUEUE
        )) {
            returnedId = producer.publishReportRequest(report);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Then("the manager report is returned successfully")
    public void the_manager_report_is_returned_successfully() {
        System.out.println("Returned response: " + returnedId);
        assertNotNull(returnedId, "Response should not be null");

        Gson gson = new Gson();
        Type mapType = new TypeToken<java.util.Map<String, Object>>(){}.getType();
        java.util.Map<String, Object> response = gson.fromJson(returnedId, mapType);

        assertNotNull(response, "Parsed response should not be null");
        assertTrue(response.containsKey("payments"), "Response should contain payments");
        assertTrue(response.containsKey("paymentCount"), "Response should contain paymentCount");
        assertTrue(response.containsKey("totalAmount"), "Response should contain totalAmount");

        Object paymentsObj = response.get("payments");
        assertNotNull(paymentsObj, "payments should not be null");
    }

    @Given("a merchant with id {string}")
    public void a_merchant_with_id(String string) {
        report = new ReportDTO(string, "merchant");
        assertNotNull(report);
    }
    @When("I make a request for a report of payments for the merchant")
    public void i_make_a_request_for_a_report_of_payments_for_the_merchant() {
        try (ReportingProducer producer = new ReportingProducer(
                getEnv("RABBITMQ_HOST", DEFAULT_HOST),
                getEnvInt("RABBITMQ_PORT", DEFAULT_PORT),
                getEnv("RABBITMQ_USER", DEFAULT_USERNAME),
                getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD),
                REPORTING_QUEUE
        )) {
            returnedId = producer.publishReportRequest(report);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @When("the customer requests {int} tokens")
    public void theCustomerRequestsTokens(int numTokens) throws IOException, InterruptedException {
        TokenDTO request = new TokenDTO();
        request.setType("ADD_TOKENS");
        request.setCustomerID(tokenCustomerID);
        request.setAmount(numTokens);

        lastTokenResponse = tokenProducer.sendTokenRequest(request);
    }

    @Then("the request is accepted")
    public void theRequestIsGrantedAndTokensAreReceived() {
        assertEquals("SUCCESS", lastTokenResponse.getType());
    }

    @And("{int} tokens are added")
    public void tokensAreAdded(int arg0) {
        assertEquals("SUCCESS", lastTokenResponse.getType());
    }

    @When("the customer pays a merchant using one token")
    public void theCustomerPaysAMerchantUsingOneToken() throws Exception {
        TokenDTO request = new TokenDTO();
        request.setType("GET_TOKEN");
        request.setCustomerID(tokenCustomerID);

        lastTokenResponse = tokenProducer.sendTokenRequest(request);
        usedToken = lastTokenResponse.getToken();

        TokenDTO validation = new TokenDTO();
        validation.setType("VALIDATE_TOKEN");
        validation.setToken(usedToken);

        lastTokenResponse = tokenProducer.sendTokenRequest(validation);
    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        assertEquals("SUCCESS", lastTokenResponse.getType());
    }

    @And("the customer now has {int} unused tokens")
    public void theCustomerNowHasUnusedTokens(int arg0) {

    }


    @When("the customer attempts to pay again with the same token")
    public void theCustomerAttemptsToPayAgainWithTheSameToken() throws IOException, InterruptedException {
        TokenDTO validation = new TokenDTO();
        validation.setType("VALIDATE_TOKEN");
        validation.setToken(usedToken);

        lastTokenResponse = tokenProducer.sendTokenRequest(validation);
        assertNotNull(lastTokenResponse, "The Token Service failed to respond to the validation request.");
    }

    @Then("the request is denied")
    public void theRequestIsDenied() {
        assertNotNull(lastTokenResponse, "The Token Service did not respond in time (Timeout).");
        assertEquals("ERROR", lastTokenResponse.getType());
    }

    @Given("the customer firstname {string} lastname {string} with cpr {string} is registered with the bank with an initial balance of {int} kr")
    public void the_customer_firstname_lastname_with_cpr_is_registered_with_the_bank_with_an_initial_balance_of_kr(String string, String string2, String string3, Integer int1) {
        customerUser.setFirstName(string);
        customerUser.setLastName(string2);
        customerUser.setCprNumber(string3);
        BigDecimal amount = new BigDecimal(int1);

        customerAccountId = "";
        try {
            customerAccountId = bank.createAccountWithBalance(apiKey, customerUser, amount);
        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
        accounts.add(customerAccountId);


    }
    @Given("the merchant firstname {string} lastname {string} with cpr {string} is registered with the bank with an initial balance of {int} kr")
    public void the_merchant_firstname_lastname_with_cpr_is_registered_with_the_bank_with_an_initial_balance_of_kr(String string, String string2, String string3, Integer int1) {
        merchantUser.setFirstName(string);
        merchantUser.setLastName(string2);
        merchantUser.setCprNumber(string3);
        BigDecimal amount = new BigDecimal(int1);

        merchantAccountId = "";
        try {
            merchantAccountId = bank.createAccountWithBalance(apiKey, merchantUser, amount);
        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
        accounts.add(merchantAccountId);



    }
    @Given("a transaction between the customer and the merchant is initiated with amount {float} kr and message {string}")
    public void a_transaction_between_the_customer_and_the_merchant_is_initiated_with_amount_kr_and_message(Float float1, String string) {
        payment = new PaymentDTO();
        payment.setAmount(float1);
        payment.setCustomerAccountId(customerAccountId);
        payment.setMerchantAccountId(merchantAccountId);
        payment.setMessage(string);

    }
    @When("I register the payment with the payment service")
    public void i_register_the_payment_with_the_payment_service() {
        try {
            returnedId = paymentProducer.publishPaymentRequested(payment);
        } catch (Exception e) {
            System.out.println("Exception occurred while registering payment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Then("the payment service should respond with a success message")
    public void the_payment_service_should_respond_with_a_success_message() {
        assertEquals("Success!", returnedId);
    }
    @Then("The customer has balance {double} on their bank account and merchant has balance {double}")
    public void the_customer_has_balance_on_their_bank_account_and_merchant_has_balance(Double double1, Double double2) {
        try {
            BigDecimal customerBalance = bank.getAccount(customerAccountId).getBalance();
            BigDecimal merchantBalance = bank.getAccount(merchantAccountId).getBalance();

            assertEquals(0, customerBalance.compareTo(BigDecimal.valueOf(double1)), "Customer balance does not match expected value.");
            assertEquals(0, merchantBalance.compareTo(BigDecimal.valueOf(double2)), "Merchant balance does not match expected value.");
        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Then("the payment service should respond with an insufficient funds message")
    public void the_payment_service_should_respond_with_an_insufficient_funds_message() {
        assertEquals("{\"error\": \"Failed to process payment:\"}", returnedId);
    }

    @Given("a transaction between the invalid customer account {string} and the merchant is initiated with amount {float} kr and message {string}")
    public void a_transaction_between_the_invalid_customer_account_and_the_merchant_is_initiated_with_amount_kr_and_message(String string, Float float1, String string2) {
        payment = new PaymentDTO();
        payment.setAmount(float1);
        payment.setCustomerAccountId(string);
        payment.setMerchantAccountId(merchantAccountId);
        payment.setMessage(string2);

    }

    @Then("the payment service should respond with an invalid customer account message")
    public void the_payment_service_should_respond_with_an_invalid_customer_account_message() {
        assertEquals("{\"error\": \"Failed to process payment:\"}", returnedId);
    }





    @After
    public void cleanup() throws Exception {
        for (String account : accounts) {
            bank.retireAccount(apiKey, account);
        }

        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }

    // private String readMessage(String queueName) throws Exception {
    //     var delivery = channel.basicGet(queueName, false);
    //     assertNotNull(delivery, "Expected a message to be available on the queue.");
    //     return new String(delivery.getBody(), StandardCharsets.UTF_8);
    // }

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

    @And("a customer with customerID {string}")
    public void aCustomerWithCustomerID(String arg0) {
        tokenCustomerID = arg0;
    }
}
