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
        customer.setCprNumber("111111-1111");
        customerAccountId = bank.createAccountWithBalance(apiKey, customer, new BigDecimal(balance));
        accounts.add(customerAccountId);
    }

    @Given("a merchant is created in the bank with balance {int} kr")
    public void aMerchantIsCreatedInTheBank(int balance) throws BankServiceException_Exception {
        merchant.setFirstName("Blackbox");
        merchant.setLastName("Merchant");
        merchant.setCprNumber("222222-2222");
        merchantAccountId = bank.createAccountWithBalance(apiKey, merchant, new BigDecimal(balance));
        accounts.add(merchantAccountId);
    }

    @And("the customer is registered with DTU Pay via the public API")
    public void theCustomerIsRegisteredViaApi() throws Exception {
        String body = String.format(
                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"cpr\":\"%s\",\"bankAccountId\":\"%s\"}",
                customer.getFirstName(), customer.getLastName(), customer.getCprNumber(), customerAccountId
        );
        HttpResponse<String> response = apiCall.post("/customer", body);
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300);
    }

    @And("the merchant is registered with DTU Pay via the public API")
    public void theMerchantIsRegisteredViaApi() throws Exception {
        String body = String.format(
                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"cpr\":\"%s\",\"bankAccountId\":\"%s\"}",
                merchant.getFirstName(), merchant.getLastName(), merchant.getCprNumber(), merchantAccountId
        );
        HttpResponse<String> response = apiCall.post("/merchant", body);
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300);
    }

    @When("the merchant initiates a payment of {int} kr via the public API")
    public void theMerchantInitiatesPayment(int amount) throws Exception {
        String body = String.format(java.util.Locale.US,
                "{\"customerAccountId\":\"%s\",\"merchantAccountId\":\"%s\",\"amount\":%.2f,\"message\":\"Blackbox payment\"}",
                customerAccountId, merchantAccountId, (float) amount
        );
        HttpResponse<String> response = apiCall.post("/payment", body);
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
