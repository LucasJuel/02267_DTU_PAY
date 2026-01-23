package org.g10;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.g10.Services.TokenService;
import org.g10.DTO.TokenDTO;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
/**
 @author Martin-Surlykke
 **/
public class TokenSteps {

    String usedToken;
    String lastIssuedToken;
    int currentTokens;
    TokenService tokenService = new TokenService();
    TokenDTO tokenDTO;
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
        lastIssuedToken = usedToken;
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

    @Given("a customer with null customerId")
    public void a_customer_with_null_customer_id() {
        customerId = null;
    }
    @When("the customer attempts to request {int} tokens")
    public void the_customer_attempts_to_request_tokens(Integer int1) {
        try {
            success = tokenService.requestAddTokens(customerId, int1);
        } catch (Exception e) {
            success = false;
        }
    }
    @Then("the request fails with an error")
    public void the_request_fails_with_an_error() {
        assertFalse(success);
    }

    @Given("a customer with empty customerId")
    public void a_customer_with_empty_customer_id() {
        customerId = "";
    }
    @When("the customer attempts to consume a token")
    public void the_customer_attempts_to_consume_a_token() {
        try {
            tokenService.consumeToken("someToken");
            success = true;
        } catch (Exception e) {
            success = false;
        }
    }

    @Given("a non-existing customer {string}")
    public void a_non_existing_customer(String string) {
        customerId = string;
    }

    @When("the customer attempts to get their token")
    public void the_customer_attempts_to_get_their_token() {
        try {
            tokenService.requestGetToken(customerId);
            success = true;
        } catch (Exception e) {
            success = false;
        }
    }

    @Given("a registered customer {string} with {int} tokens")
    public void a_registered_customer_with_tokens(String string, Integer int1) {
        customerId = string;
        tokenService.removeAllCustomerTokens(customerId);
        tokenService.requestAddTokens(customerId, int1);
    }
    @When("all tokens are removed for the customer")
    public void all_tokens_are_removed_for_the_customer() {
        tokenService.removeAllCustomerTokens(customerId);
    }

    @Then("the customer has zero tokens")
    public void the_customer_has_zero_tokens() {
        assertEquals(0, tokenService.getNumTokens(customerId));
    }

    @When("a Token DTO is created with these values")
    public void a_token_dto_is_created_with_these_values() {
        if (tokenDTO == null) {
            tokenDTO = new TokenDTO();
        }
    }
    
    @Given("a token value {string} and customerId {string}")
    public void a_token_value_and_customer_id(String string, String string2) {
        if (tokenDTO == null) {
            tokenDTO = new TokenDTO();
        }
        tokenDTO.setToken(string);
        tokenDTO.setCustomerID(string2);
    }
    
    @Given("an amount of {int} tokens, type {string}, and error message {string}")
    public void an_amount_of_tokens_type_and_error_message(Integer int1, String string, String string2) {
        if (tokenDTO == null) {
            tokenDTO = new TokenDTO();
        }
        tokenDTO.setAmount(int1);
        tokenDTO.setType(string);
        tokenDTO.setErrorMSG(string2);
    }
    
    @Then("the Token DTO has")
    public void the_token_dto_has() {
        assertNotNull(tokenDTO.getAmount());
        assertNotNull(tokenDTO.getCustomerID());
        assertNotNull(tokenDTO.getErrorMSG());
        assertNotNull(tokenDTO.getToken());
        assertNotNull(tokenDTO.getType());
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


