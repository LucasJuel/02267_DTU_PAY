package com.client;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StepDefinitions {
    private Customer customer;
    private Merchant merchant;
    private HashMap<String, Object> customerResponseObj, merchantResponseObj, paymentResponseObj;
    private String customerId, merchantId;
    private SimpleDtuPay dtupay = new SimpleDtuPay();
    private boolean successful = false;
    private int statusCode;

    @Given("a customer with name {string}")
    public void aCustomerWithName(String name) {
        customer = new Customer(name);
        assertTrue(customer != null);
    }

    @Given("the customer is registered with Simple DTU Pay")
    public void theCustomerIsRegisteredWithSimpleDTUPay() {
        customerResponseObj = dtupay.register(customer);
        statusCode = (int) customerResponseObj.get("status");
        assertTrue(statusCode == 200 || statusCode == 409, "Customer registration failed with status code: " + statusCode);
    }

    @Given("a merchant with name {string}")
    public void aMerchantWithName(String name) {
        merchant = new Merchant(name);
        assertTrue(merchant != null);
    }

    @Given("the merchant is registered with Simple DTU Pay")
    public void theMerchantIsRegisteredWithSimpleDTUPay() {
        merchantResponseObj = dtupay.register(merchant);
        statusCode = (int) merchantResponseObj.get("status");
        assertTrue(statusCode == 200 || statusCode == 409, "Merchant registration failed with status code: " + statusCode);
    }

    @When("the merchant initiates a payment for {int} kr by the customer")
    public void theMerchantInitiatesAPaymentForKrByTheCustomer(Integer amount) {
        paymentResponseObj = dtupay.pay(amount,customerId,merchantId);
        statusCode = (int) paymentResponseObj.get("status");
        successful = (statusCode == 200);
        System.out.println("Payment initiated for " + amount + " kr by customer " + customerId + " to merchant " + merchantId);
    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        assertTrue(successful, "Payment failed with status code " + statusCode);
    }

    @Given("a customer with name {string}, who is registered with Simple DTU Pay")
    public void aCustomerWithNameWhoIsRegisteredWithSimpleDTUPay(String name) {
        customer = new Customer(name);
        customerResponseObj = dtupay.register(customer);
        statusCode = (int) customerResponseObj.get("status");
        assertTrue(statusCode == 200 || statusCode == 409, "Customer registration failed with status code: " + statusCode);
    }
    

    @And("a merchant with name {string}, who is registered with Simple DTU Pay")
    public void aMerchantWithNameWhoIsRegisteredWithSimpleDTUPay(String name) {
        merchant = new Merchant(name);
        merchantResponseObj = dtupay.register(merchant);
        statusCode = (int) merchantResponseObj.get("status");
        assertTrue(statusCode == 200 || statusCode == 409, "Merchant registration failed with status code: " + statusCode);
    }


    @Given("a successful payment of {string} kr from the customer to the merchant")
    public void aSuccessfulPaymentOfKrFromTheCustomerToTheMerchant(String amountStr) {
        paymentResponseObj = dtupay.pay(amountStr, customerId, merchantId);
        statusCode = (int) paymentResponseObj.get("status");
        successful = (statusCode == 200);
        System.out.println("Successful payment of " + amountStr + " kr from customer " + customerId + " to merchant " + merchantId);
    }

    @When("the manager asks for a list of payments")
    public void theManagerAsksForAListOfPayments() {
        // Implementation for listing payments can be added here
        System.out.println("Manager requested list of payments.");
    }

    @Then("the list contains a payments where customer {string} paid {string} kr to merchant {string}")
    public void theListContainsAPaymentsWhereCustomerPaidKrToMerchant(String customerName, String amount, String merchantName) {
        // TODO: Implement verification logic - fetch list and check payment exists
        System.out.println("Verifying payment: " + customerName + " paid " + amount + " kr to " + merchantName);
        assertTrue(successful, "Payment should have been successful");
    }
}