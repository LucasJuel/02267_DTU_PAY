package com.client;

import com.client.utils.ApiCall;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import dtu.ws.fastmoney.User;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 @author LucasJuel
 **/
public class OuterBlackboxSteps {
    private static final int MAX_RETRIES = 12;
    private static final long RETRY_DELAY_MS = 1000;

    private final BankService bank = new BankService_Service().getBankServicePort();
    private final String apiKey = "kayak2098";
    private final List<String> accounts = new ArrayList<>();

    private final ApiCall apiCall;

    private final User customer = new User();
    private final User merchant = new User();
    private String customerAccountId;
    private String merchantAccountId;
    private String token;
    private String customerDTUpayID;
    HttpResponse<String> response;

    public OuterBlackboxSteps() {
        String envUrl = System.getenv("SERVER_URL");
        String baseUrl = (envUrl != null) ? envUrl : "http://localhost:8080";
        this.apiCall = new ApiCall(baseUrl);
    }

    @Given("the DTU Pay system is running")
    public void theSystemIsRunning() {
        // No-op: environment is expected to be up (docker-compose in CI or local).
    }

    @Given("a customer is created in the bank with balance {int} kr")
    public void aCustomerIsCreatedInTheBank(int balance) throws BankServiceException_Exception {
        customer.setFirstName("Blackbox");
        customer.setLastName("Customer");
        customer.setCprNumber("115928-1111");
        customerAccountId = bank.createAccountWithBalance(apiKey, customer, new BigDecimal(balance));
        accounts.add(customerAccountId);
    }

    @Given("a merchant is created in the bank with balance {int} kr")
    public void aMerchantIsCreatedInTheBank(int balance) throws BankServiceException_Exception {
        merchant.setFirstName("Blackbox");
        merchant.setLastName("Merchant");
        merchant.setCprNumber("222204-2222");
        merchantAccountId = bank.createAccountWithBalance(apiKey, merchant, new BigDecimal(balance));
        accounts.add(merchantAccountId);
    }

    @And("the customer is registered with DTU Pay via the public API")
    public void theCustomerIsRegisteredViaApi() throws Exception {
        String body = String.format(
                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"cpr\":\"%s\",\"bankAccountId\":\"%s\"}",
                customer.getFirstName(), customer.getLastName(), customer.getCprNumber(), customerAccountId
        );
        response = apiCall.post("/customer", body);
        customerDTUpayID = response.body();
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300);
    }

    @And("the merchant is registered with DTU Pay via the public API")
    public void theMerchantIsRegisteredViaApi() throws Exception {
        String body = String.format(
                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"cpr\":\"%s\",\"bankAccountId\":\"%s\"}",
                merchant.getFirstName(), merchant.getLastName(), merchant.getCprNumber(), merchantAccountId
        );
        response = apiCall.post("/merchant", body);
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300);
    }

    @And("the customer provides the merchant with a token for payment") 
    public void theCustomerProvidesToken() throws Exception {

        response = apiCall.get("/token/" + customerDTUpayID);
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300);
        token = response.body();
    }

    @When("the merchant initiates a payment of {int} kr via the public API")
    public void theMerchantInitiatesPayment(int amount) throws Exception {
        String body = String.format(java.util.Locale.US,
                "{\"customerToken\":\"%s\",\"merchantAccountId\":\"%s\",\"amount\":%.2f,\"message\":\"Blackbox payment\"}",
                token, merchantAccountId, (float) amount
        );
        HttpResponse<String> response = apiCall.post("/payment", body);
        System.out.println("Payment request response LOOK HERE: " + response.body());
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300);
    }

    @Then("the customer bank balance eventually becomes {int} kr")
    public void theCustomerBalanceEventuallyBecomes(int expected) throws Exception {
        awaitBalance(customerAccountId, new BigDecimal(expected));
    }

    @And("the merchant bank balance eventually becomes {int} kr")
    public void theMerchantBalanceEventuallyBecomes(int expected) throws Exception {
        awaitBalance(merchantAccountId, new BigDecimal(expected));
    }

    @When("the customer requests a new set of {int} tokens via the public API")
    public void the_customer_requests_tokens_via_the_public_api(Integer int1) throws Exception {
        String body = String.format(
        "{\"customerID\":\"%s\" ,\"amount\":%d}",
        customerDTUpayID, int1
    );
        response = apiCall.post("/token", body);
    }
    @Then("the token request is rejected with an error message indicating token limit exceeded")
    public void the_token_request_is_rejected_with_an_error_message_indicating_token_limit_exceeded() {     
        assertEquals("{\"type\":\"ERROR\",\"customerID\":null,\"amount\":0,\"token\":null,\"errorMSG\":\"ADD TOKENS FAILED\"}", response.body());
    }

    @When("the merchant initiates a payment of {int} kr via the public API with the invalid token {string}")
    public void the_merchant_initiates_a_payment_of_kr_via_the_public_api_with_the_invalid_token(Integer int1, String string) {
        String body = String.format(java.util.Locale.US,
                "{\"customerToken\":\"%s\",\"merchantAccountId\":\"%s\",\"amount\":%.2f,\"message\":\"Blackbox payment\"}",
                string, merchantAccountId, (float) int1
        );
        try {
            response = apiCall.post("/payment", body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Payment request response LOOK HERE: " + response.body());

    }
    @Then("the payment is rejected with an error message indicating invalid token")
    public void the_payment_is_rejected_with_an_error_message_indicating_invalid_token() {

    }

    private void awaitBalance(String accountId, BigDecimal expected) throws Exception {
        for (int i = 0; i < MAX_RETRIES; i++) {
            BigDecimal balance = bank.getAccount(accountId).getBalance();
            if (expected.compareTo(balance) == 0) {
                return;
            }
            Thread.sleep(RETRY_DELAY_MS);
        }
        BigDecimal finalBalance = bank.getAccount(accountId).getBalance();
        assertEquals(0, expected.compareTo(finalBalance));
    }

    @After
    public void deleteAccounts() throws BankServiceException_Exception {
        for (String account : accounts) {
            bank.retireAccount(apiKey, account);
        }
    }
}
