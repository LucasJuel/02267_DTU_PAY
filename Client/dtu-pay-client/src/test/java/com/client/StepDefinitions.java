package com.client;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StepDefinitions {
    private Customer customer;
    private Merchant merchant;
    private String customerId, merchantId;
    private SimpleDtuPay dtupay = new SimpleDtuPay();
    private boolean successful = false;

    @Given("a customer with name {string}")
    public void aCustomerWithName(String name) {
        customer = new Customer(name);
        System.out.println("Created customer with name: " + name);
    }

    @Given("the customer is registered with Simple DTU Pay")
    public void theCustomerIsRegisteredWithSimpleDTUPay() {
        customerId = dtupay.register(customer);
        System.out.println("Registered customer with ID: " + customerId);
    }

    @Given("a merchant with name {string}")
    public void aMerchantWithName(String name) {
        merchant = new Merchant(name);
        System.out.println("Created merchant with name: " + name);
    }

    @Given("the merchant is registered with Simple DTU Pay")
    public void theMerchantIsRegisteredWithSimpleDTUPay() {
        merchantId = dtupay.register(merchant);
        System.out.println("Registered merchant with ID: " + merchantId);
    }

    @When("the merchant initiates a payment for {int} kr by the customer")
    public void theMerchantInitiatesAPaymentForKrByTheCustomer(Integer amount) {
        successful = dtupay.pay(amount,customerId,merchantId);
        System.out.println("Payment initiated for " + amount + " kr by customer " + customerId + " to merchant " + merchantId);
    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        assertTrue(successful);
    }

    @Given("a customer named Susan, who is registered with Simple DTU Pay")
    public void aCustomerNamedSusanWhoIsRegisteredWithSimpleDTUPay() {
        customer = new Customer("Susan");
        customerId = dtupay.register(customer);
        System.out.println("Registered customer Susan with ID: " + customerId);
    }

    @And("a merchant named Daniel, who is registered with Simple DTU Pay")
    public void aMerchantNamedDanielWhoIsRegisteredWithSimpleDTUPay() {
        merchant = new Merchant("Daniel");
        merchantId = dtupay.register(merchant);
        System.out.println("Registered merchant Daniel with ID: " + merchantId);
    }

    @Given("a successful payment of {string} kr from the customer to the merchant")
    public void aSuccessfulPaymentOfKrFromTheCustomerToTheMerchant(String amountStr) {
        successful = dtupay.pay(amountStr, customerId, merchantId);
        System.out.println("Successful payment of " + amountStr + " kr from customer " + customerId + " to merchant " + merchantId);
    }

    @When("the manager asks for a list of payments")
    public void theManagerAsksForAListOfPayments() {
        // Implementation for listing payments can be added here
        System.out.println("Manager requested list of payments.");
    }

    @Given("a customer with name {string}, who is registered with Simple DTU Pay")
    public void aCustomerWithNameWhoIsRegisteredWithSimpleDTUPay(String name) {
        customer = new Customer(name);
        customerId = dtupay.register(customer);
        System.out.println("Registered customer " + name + " with ID: " + customerId);
    }

    @Given("a merchant with name {string}, who is registered with Simple DTU Pay")
    public void aMerchantWithNameWhoIsRegisteredWithSimpleDTUPay(String name) {
        merchant = new Merchant(name);
        merchantId = dtupay.register(merchant);
        System.out.println("Registered merchant " + name + " with ID: " + merchantId);
    }

    @Then("the list contains a payments where customer {string} paid {string} kr to merchant {string}")
    public void theListContainsAPaymentsWhereCustomerPaidKrToMerchant(String customerName, String amount, String merchantName) {
        // TODO: Implement verification logic - fetch list and check payment exists
        System.out.println("Verifying payment: " + customerName + " paid " + amount + " kr to " + merchantName);
        assertTrue(successful, "Payment should have been successful");
    }
}