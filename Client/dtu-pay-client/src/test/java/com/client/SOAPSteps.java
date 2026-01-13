package com.client;

import dtu.ws.fastmoney.*;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class SOAPSteps {

    private final BankService bank = new BankService_Service().getBankServicePort();

    List<String> accounts = new ArrayList<>();

    private final String apiKey = "kayak2098";

    private final SimpleDtuPay simpleDtuPay = new SimpleDtuPay();

    // State for Customer
    private final User customer = new User();

    // State for Merchant
    private final User merchant = new User();

    private String customerAccount;
    private String merchantAccount;
    private HashMap<String, Object> lastCustomerRegistrationResponse;
    private HashMap<String, Object> lastMerchantRegistrationResponse;
    private HashMap<String, Object> lastPaymentResponse;

    @Given("a customer with name {string}, last name {string}, and CPR {string}")
    public void aCustomerWithNameLastNameAndCPR(String arg0, String arg1, String arg2) {
        customer.setFirstName(arg0);
        customer.setLastName(arg1);
        customer.setCprNumber(arg2);

        assertEquals(arg0, customer.getFirstName());
        assertEquals(arg1, customer.getLastName());
        assertEquals(arg2, customer.getCprNumber());
    }

    @And("the customer is registered with the bank with an initial balance of {int} kr")
    public void theCustomerIsRegisteredWithTheBankWithAnInitialBalanceOfKr(int arg0) throws BankServiceException_Exception {
        System.out.println("Trying to create bank account for customer: " + customer.getFirstName() + " " + customer.getLastName() + ", CPR: " + customer.getCprNumber());
        customerAccount = bank.createAccountWithBalance(apiKey, customer, new BigDecimal(arg0));
        System.out.println("Created customer bank account: " + customerAccount);
        accounts.add(customerAccount);

        assert (bank.getAccount(customerAccount).getBalance().equals(new BigDecimal(arg0)));
    }

    @And("the customer is registered with Simple DTU Pay using their bank account")
    public void theCustomerIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() throws BankServiceException_Exception {

        lastCustomerRegistrationResponse = simpleDtuPay.registerCustomerWithResponse(customer, customerAccount);
        assertNotNull(lastCustomerRegistrationResponse);
        assertTrue((int) lastCustomerRegistrationResponse.get("status") == 200
                || (int) lastCustomerRegistrationResponse.get("status") == 201);
        assertEquals(customer.getFirstName(), bank.getAccount(customerAccount).getUser().getFirstName());

    }

    @And("a merchant with name {string}, last name {string}, and CPR {string}")
    public void aMerchantWithNameLastNameAndCPR(String name, String lastName, String cpr) {
        merchant.setFirstName(name);
        merchant.setLastName(lastName);
        merchant.setCprNumber(cpr);

        assertEquals(merchant.getFirstName(), name);
        assertEquals(merchant.getLastName(), lastName);
        assertEquals(merchant.getCprNumber(), cpr);

    }

    @And("the merchant is registered with the bank with an initial balance of {int} kr")
    public void theMerchantIsRegisteredWithTheBankWithAnInitialBalanceOfKr(int initBalance) throws BankServiceException_Exception {
        BigDecimal balance = new BigDecimal(initBalance);
        System.out.println("Trying to create bank account for merchant: " + merchant.getFirstName() + " " + merchant.getLastName() + ", CPR: " + merchant.getCprNumber());
        merchantAccount = bank.createAccountWithBalance(apiKey, merchant, balance);
        System.out.println("Created merchant bank account: " + merchantAccount);

        accounts.add(merchantAccount);

        assert (Objects.equals(bank.getAccount(merchantAccount).getBalance(), balance));
    }

    @And("the merchant is registered with Simple DTU Pay using their bank account")
    public void theMerchantIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() throws BankServiceException_Exception {
        lastMerchantRegistrationResponse = simpleDtuPay.registerMerchantWithResponse(merchant, merchantAccount);
        assertNotNull(lastMerchantRegistrationResponse);
        assertTrue((int) lastMerchantRegistrationResponse.get("status") == 200
                || (int) lastMerchantRegistrationResponse.get("status") == 201);

        assertEquals(merchant.getFirstName(), bank.getAccount(merchantAccount).getUser().getFirstName());
    }

    @And("the customer uses bank account id {string}")
    public void theCustomerUsesBankAccountId(String accountId) {
        customerAccount = accountId;
    }

    @And("the merchant uses bank account id {string}")
    public void theMerchantUsesBankAccountId(String accountId) {
        merchantAccount = accountId;
    }

    @When("the customer attempts to register with Simple DTU Pay using their bank account")
    public void theCustomerAttemptsToRegisterWithSimpleDTUPayUsingTheirBankAccount() {
        lastCustomerRegistrationResponse = simpleDtuPay.registerCustomerWithResponse(customer, customerAccount);
    }

    @When("the customer attempts to register with Simple DTU Pay using bank account {string}")
    public void theCustomerAttemptsToRegisterWithSimpleDTUPayUsingBankAccount(String accountId) {
        lastCustomerRegistrationResponse = simpleDtuPay.registerCustomerWithResponse(customer, accountId);
    }

    @When("the merchant attempts to register with Simple DTU Pay using their bank account")
    public void theMerchantAttemptsToRegisterWithSimpleDTUPayUsingTheirBankAccount() {
        lastMerchantRegistrationResponse = simpleDtuPay.registerMerchantWithResponse(merchant, merchantAccount);
    }

    @When("the merchant attempts to register with Simple DTU Pay using bank account {string}")
    public void theMerchantAttemptsToRegisterWithSimpleDTUPayUsingBankAccount(String accountId) {
        lastMerchantRegistrationResponse = simpleDtuPay.registerMerchantWithResponse(merchant, accountId);
    }

    @And("the customer details are changed to name {string}, last name {string}, and CPR {string}")
    public void theCustomerDetailsAreChangedToNameLastNameAndCPR(String name, String lastName, String cpr) {
        customer.setFirstName(name);
        customer.setLastName(lastName);
        customer.setCprNumber(cpr);
    }

    @And("the merchant details are changed to name {string}, last name {string}, and CPR {string}")
    public void theMerchantDetailsAreChangedToNameLastNameAndCPR(String name, String lastName, String cpr) {
        merchant.setFirstName(name);
        merchant.setLastName(lastName);
        merchant.setCprNumber(cpr);
    }

    @Then("the customer registration is successful")
    public void theCustomerRegistrationIsSuccessful() {
        assertNotNull(lastCustomerRegistrationResponse);
        assertTrue((int) lastCustomerRegistrationResponse.get("status") == 200
                || (int) lastCustomerRegistrationResponse.get("status") == 201);
    }

    @Then("the merchant registration is successful")
    public void theMerchantRegistrationIsSuccessful() {
        assertNotNull(lastMerchantRegistrationResponse);
        assertTrue((int) lastMerchantRegistrationResponse.get("status") == 200
                || (int) lastMerchantRegistrationResponse.get("status") == 201);
    }

    @Then("the customer registration fails with status {int}")
    public void theCustomerRegistrationFailsWithStatus(int status) {
        assertNotNull(lastCustomerRegistrationResponse);
        assertEquals(status, lastCustomerRegistrationResponse.get("status"));
    }

    @Then("the merchant registration fails with status {int}")
    public void theMerchantRegistrationFailsWithStatus(int status) {
        assertNotNull(lastMerchantRegistrationResponse);
        assertEquals(status, lastMerchantRegistrationResponse.get("status"));
    }

    @And("the balance of the customer at the bank is {int} kr")
    public void theBalanceOfTheCustomerAtTheBankIsKr(int arg0) throws BankServiceException_Exception {
        assertEquals(bank.getAccount(customerAccount).getBalance(), new BigDecimal(arg0));

    }


    @And("the balance of the merchant at the bank is {int} kr")
    public void theBalanceOfTheMerchantAtTheBankIsKr(int arg0) throws BankServiceException_Exception {
        assertEquals(bank.getAccount(merchantAccount).getBalance(), new BigDecimal(arg0));
    }

    @After
    public void deleteAccounts() throws BankServiceException_Exception {
        for (String account : accounts) {
            bank.retireAccount(apiKey, account);
        }
    }

    @When("the SOAP merchant initiates a payment for {int} kr by the customer")
    public void theSOAPMerchantInitiatesAPaymentForKrByTheCustomer(int amount) {
        float amountFloat = (float) amount;
        lastPaymentResponse = simpleDtuPay.registerPayment(amountFloat, customerAccount, merchantAccount, "TEST");
    }

    @When("the SOAP merchant initiates a payment for {int} kr by the customer with the description {string}")
    public void theSOAPMerchantInitiatesAPaymentForKrByTheCustomerWithTheDescription(int amount, String desc) {
        float amountFloat = (float) amount;
        lastPaymentResponse = simpleDtuPay.registerPayment(amountFloat, customerAccount, merchantAccount, desc);
    }

    @Then("the SOAP payment fails with status {int}")
    public void theSOAPPaymentFailsWithStatus(int status) {
        assertNotNull(lastPaymentResponse);
        assertEquals(status, lastPaymentResponse.get("status"));
    }

    @Then("there does not exists a SOAP payment with the description {string}")
    public Object thereDoesNotExistsASAOPPaymentWithTheDescription(String arg0) throws BankServiceException_Exception {

        List<Transaction> transactions = bank.getAccount(merchantAccount).getTransactions();
        for (Transaction trans : transactions) {
            System.out.println(trans.getDescription());
            if (Objects.equals(trans.getDescription(), arg0)) {
                return fail();
            }
        }
        return null;
    }

    @Then("there exists a SOAP payment with the description {string}")
    public Object thereExistsASAOPPaymentWithTheDescription(String arg0) throws BankServiceException_Exception {
        List<Transaction> transactions = bank.getAccount(merchantAccount).getTransactions();
        for (Transaction trans : transactions) {
            System.out.println(trans.getDescription());
            if (Objects.equals(trans.getDescription(), arg0)) {
                return null;
            }
        }
        return fail();
    }
}
