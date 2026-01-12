package com.client;

import dtu.ws.fastmoney.*;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Given("a customer with name {string}, last name {string}, and CPR {string}")
    public void aCustomerWithNameLastNameAndCPR(String arg0, String arg1, String arg2)  {
        customer.setFirstName(arg0);
        customer.setLastName(arg1);
        customer.setCprNumber(arg2);

        assertEquals(arg0, customer.getFirstName());
        assertEquals(arg1, customer.getLastName());
        assertEquals(arg2, customer.getCprNumber());
    }

    @And("the customer is registered with the bank with an initial balance of {int} kr")
    public void theCustomerIsRegisteredWithTheBankWithAnInitialBalanceOfKr(int arg0) throws BankServiceException_Exception {

        customerAccount = bank.createAccountWithBalance(apiKey, customer, new BigDecimal(arg0));
        accounts.add(customerAccount);

        assert(bank.getAccount(customerAccount).getBalance().equals(new BigDecimal(arg0)));
    }

    @And("the customer is registered with Simple DTU Pay using their bank account")
    public void theCustomerIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() throws BankServiceException_Exception {

        simpleDtuPay.register(customer, customerAccount);
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
        merchantAccount = bank.createAccountWithBalance(apiKey, merchant, balance);

        accounts.add(merchantAccount);

        assert(Objects.equals(bank.getAccount(merchantAccount).getBalance(), balance));
    }

    @And("the merchant is registered with Simple DTU Pay using their bank account")
    public void theMerchantIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() throws BankServiceException_Exception {
        simpleDtuPay.registerMerchantFromBankAccount(merchant, merchantAccount);

        assertEquals(merchant.getFirstName(), bank.getAccount(merchantAccount).getUser().getFirstName());
    }

    @When("the SOAP merchant initiates a payment for {int} kr by the customer")
    public void theMerchantInitiatesAPaymentFor10KrByTheCustomer(int num){
        simpleDtuPay.pay(new BigDecimal(num), customerAccount, merchantAccount, "TEST");
    }

    @Then("the SOAP payment is successful")
    public Object theSOAPPaymentIsSuccessful() throws BankServiceException_Exception {
        List<Transaction> transactions =  bank.getAccount(merchantAccount).getTransactions();
        for (Transaction trans: transactions){
            System.out.println(trans.getDescription());
            if(Objects.equals(trans.getDescription(), "TEST")){
                return true;
            }
        }
        return fail();
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

    @Given("a customer with name {string}, last name {string}, and CPR {string}, who is registered with Simple DTU Pay")
    public void aCustomerWithNameLastNameAndCPRWhoIsRegisteredWithSimpleDTUPay(String arg0, String arg1, String arg2) {

    }
}
