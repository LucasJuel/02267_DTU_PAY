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
/**
 @author LucasJuel
 **/
public class TokenSteps {

    String usedToken;

    int currentTokens;
    TokenService tokenService = new TokenService();
    String customerId = "Customer123";
    boolean success;
    @Before
    public void before() {
        tokenService.clearStorage();
    }

    @Given("a registered customer {string} without tokens")
    public void aRegisteredCustomerWithoutTokens(String cID) {
        tokenService.removeAllCustomerTokens(cID);
        currentTokens = 0;
        customerId = cID;
        assertEquals(0, tokenService.getNumTokens(customerId));
    }

    @When("the customer requests {int} tokens")
    public void theCustomerRequestsTokens(int numTokens) throws IOException, InterruptedException {

        currentTokens = tokenService.getNumTokens(customerId);
        success = tokenService.requestAddTokens(customerId, numTokens);
    }

    @Then("the request is accepted")
    public void theRequestIsGrantedAndTokensAreReceived() {

        assert(success);
    }

    @And("{int} tokens are added")
    public void tokensAreAdded(int arg0) {
        assertEquals(arg0 + currentTokens, tokenService.getNumTokens(customerId));
    }

    @When("the customer pays a merchant using one token")
    public void theCustomerPaysAMerchantUsingOneToken() throws Exception {
        usedToken = tokenService.requestGetToken(customerId);
        try {
            tokenService.consumeToken(usedToken);
            success = true;
        } catch (Exception e) {
            success = false;
        }
    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        assert(success);
    }

    @And("the customer now has {int} unused tokens")
    public void theCustomerNowHasUnusedTokens(int arg0) {
        assertEquals(arg0, tokenService.getNumTokens(customerId));
    }

    @And("the customer has used a token for a successful payment")
    public void aCustomerHasUsedATokenForASuccessfulPayment() throws Exception {
        tokenService.requestAddTokens(customerId, 2);

        usedToken = tokenService.getToken(customerId);

        tokenService.consumeToken(usedToken);
    }

    @When("the customer attempts to pay again with the same token")
    public void theCustomerAttemptsToPayAgainWithTheSameToken() throws Exception {
        try {
            tokenService.consumeToken(usedToken);
            success = true;
        } catch (Exception e) {
            success = false;
        }
    }

    @Then("the request is denied")
    public void theRequestIsDenied() {
        assert(!success);
    }

    @Given("a registered merchant")
    public void aRegisteredMerchant() {
        // We assume the merchant is correctly registered in this test
    }


    @When("the merchant attempts to process a payment with token {string}")
    public void theMerchantAttemptsToProcessAPaymentWithToken(String token) throws Exception {
        try {
            tokenService.consumeToken(token);
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
    }
}


