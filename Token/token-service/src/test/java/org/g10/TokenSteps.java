package org.g10;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.g10.DTO.TokenDTO;
import org.g10.Services.TokenProducer;
import org.g10.Services.TokenService;
import org.g10.Services.TokenServiceApplication;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class TokenSteps {

    TokenProducer producer = new TokenProducer();

    TokenDTO lastResponse;

    String usedToken;

    int currentTokens;
    TokenService tokenService = new TokenService();
    String customerId = "Customer123";
    ConnectionFactory factory;
    Connection connection;
    Channel channel;
    int rabbitPort;
    private static boolean started = false;

    public TokenSteps() throws IOException, TimeoutException {
    }

    @Before
    public void setup() throws IOException, TimeoutException {

        if (!started) {
            Thread thread = new Thread(() -> {
                try {
                    TokenServiceApplication.main(new String[]{});
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            });
            thread.setDaemon(true);
            thread.start();
            started = true;

            try { Thread.sleep(2000); } catch (Exception ignored) {}
        }
        String rabbitHost = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");
        String rabbitPortStr = System.getenv("RABBITMQ_PORT");
        rabbitPort = 5672;
        if (rabbitPortStr != null) {
            try {
                rabbitPort = Integer.parseInt(rabbitPortStr);
            } catch (NumberFormatException ignored) {
            }
        }
        String rabbitUsername = System.getenv().getOrDefault("RABBITMQ_USERNAME", "guest");

        String rabbitPassword = System.getenv().getOrDefault("RABBITMQ_PASSWORD", "guest");

        factory = new ConnectionFactory();

        factory.setHost(rabbitHost);

        factory.setPort(rabbitPort);

        factory.setUsername(rabbitUsername);

        factory.setPassword(rabbitPassword);

        connection = factory.newConnection();
        connection.createChannel();
        channel = connection.createChannel();
        channel.queueDeclare("token.requests", true, false, false, null);
    }
    @Given("a registered customer {string} without tokens")
    public void aRegisteredCustomerWithoutTokens(String cID) {
        tokenService.removeAllCustomerTokens(cID);
        currentTokens = 0;
        customerId = cID;
        int val = tokenService.getNumTokens(customerId);
        assertEquals(0, val);
    }

    @When("the customer requests {int} tokens")
    public void theCustomerRequestsTokens(int numTokens) throws IOException, InterruptedException {
        currentTokens = tokenService.getNumTokens(customerId);
        TokenDTO request = new TokenDTO();
        request.setType("ADD_TOKENS");
        request.setCustomerID(customerId);
        request.setAmount(numTokens);

        lastResponse = producer.sendTokenRequest(request);
    }

    @Then("the request is accepted")
    public void theRequestIsGrantedAndTokensAreReceived() {
        assertEquals("SUCCESS", lastResponse.getType());
    }

    @And("{int} tokens are added")
    public void tokensAreAdded(int arg0) {
        assertEquals(arg0 + currentTokens, tokenService.getNumTokens(customerId));
    }

    @When("the customer pays a merchant using one token")
    public void theCustomerPaysAMerchantUsingOneToken() throws Exception {
        usedToken = tokenService.requestGetToken(customerId);
        TokenDTO validation = new TokenDTO();
        validation.setType("VALIDATE_TOKEN");
        validation.setToken(usedToken);

        lastResponse = producer.sendTokenRequest(validation);
    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        assertEquals("SUCCESS", lastResponse.getType());
    }

    @And("the customer now has {int} unused tokens")
    public void theCustomerNowHasUnusedTokens(int arg0) {
        assertEquals(arg0, tokenService.getNumTokens(customerId));
    }

    @Given("a customer has used a token for a successful payment")
    public void aCustomerHasUsedATokenForASuccessfulPayment() throws Exception {
        TokenDTO request = new TokenDTO();
        request.setType("ADD_TOKENS");
        request.setCustomerID(customerId);
        request.setAmount(1);
        producer.sendTokenRequest(request);

        usedToken = tokenService.requestGetToken(customerId);
        assertNotNull(usedToken, "Token was not generated");

        TokenDTO validation = new TokenDTO();
        validation.setType("VALIDATE_TOKEN");
        validation.setToken(usedToken);
        lastResponse = producer.sendTokenRequest(validation);

        assertEquals("SUCCESS", lastResponse.getType(), lastResponse.getErrorMSG());

    }

    @When("the customer attempts to pay again with the same token")
    public void theCustomerAttemptsToPayAgainWithTheSameToken() throws IOException, InterruptedException {
        TokenDTO validation = new TokenDTO();
        validation.setType("VALIDATE_TOKEN");
        validation.setToken(usedToken);

        lastResponse = producer.sendTokenRequest(validation);
    }

    @Then("the request is denied")
    public void theRequestIsDenied() {
        assertEquals("ERROR", lastResponse.getType());
    }

    @Given("a registered merchant")
    public void aRegisteredMerchant() {
        // We assume the merchant is correctly registered in this test
    }


    @When("the merchant attempts to process a payment with token {string}")
    public void theMerchantAttemptsToProcessAPaymentWithToken(String token) throws IOException, InterruptedException {
        TokenDTO request = new TokenDTO();
        request.setType("VALIDATE_TOKEN");
        request.setToken(token);
        lastResponse = producer.sendTokenRequest(request);

    }

    @After
    public void tearDown() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }
}


