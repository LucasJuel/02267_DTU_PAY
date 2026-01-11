package com.client;

import dtu.ws.fastmoney.*;
import io.cucumber.java.After;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SOAPSteps {

    private BankService bank;

    List<String> accounts = new ArrayList<>();

    private final String apiKey = "kayak2098";

    private SimpleDtuPay simpleDtuPay = new SimpleDtuPay();

    // State for Customer
    private User customer;
    private String customerBankId;
    private String customerDtuPayId;

    // State for Merchant
    private User merchant;
    private String merchantBankId;
    private String merchantDtuPayId;

    private String account;

    @Given("a customer with name {string}, last name {string}, and CPR {string}")
    public void aCustomerWithNameLastNameAndCPR(String arg0, String arg1, String arg2)  {
        customer.setFirstName(arg0);
        customer.setLastName(arg1);
        customer.setCprNumber(arg2);
    }

    @And("the customer is registered with the bank with an initial balance of {int} kr")
    public void theCustomerIsRegisteredWithTheBankWithAnInitialBalanceOfKr(int arg0) throws BankServiceException_Exception {

        account = bank.createAccountWithBalance(apiKey, customer, new BigDecimal(arg0));
        accounts.add(account);

    }

    @And("the customer is registered with Simple DTU Pay using their bank account")
    public void theCustomerIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {

        simpleDtuPay.registerUserFromBankAccount(customer, account);

    }

    @And("a merchant with name {string}, last name {string}, and CPR {string}")
    public void aMerchantWithNameLastNameAndCPR(String arg0, String arg1, String arg2) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("the merchant is registered with the bank with an initial balance of {int} kr")
    public void theMerchantIsRegisteredWithTheBankWithAnInitialBalanceOfKr(int arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("the merchant is registered with Simple DTU Pay using their bank account")
    public void theMerchantIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("the SOAP merchant initiates a payment for 10 kr by the customer")
    public void theMerchantInitiatesAPaymentFor10KrByTheCustomer() {
        throw new PendingException();
    }

    @Then("the SOAP payment is successful")
    public void theSOAPPaymentIsSuccessful() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }


    @And("the balance of the customer at the bank is {int} kr")
    public void theBalanceOfTheCustomerAtTheBankIsKr(int arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }


    @And("the balance of the merchant at the bank is {int} kr")
    public void theBalanceOfTheMerchantAtTheBankIsKr(int arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @After("Delete created accounts")
    public void deleteAccounts() throws BankServiceException_Exception {
        for (String account : accounts) {
            bank.retireAccount(apiKey, account);
        }
    }
}
