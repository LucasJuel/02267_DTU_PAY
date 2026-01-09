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
    private String payments;
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
        customerId = "customer-id-" + customer.getName();
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
        merchantId = "merchant-id-" + merchant.getName();
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
        customerId = "customer-id-" + customer.getName();
        assertTrue(statusCode == 200 || statusCode == 409, "Customer registration failed with status code: " + statusCode);
    }
    

    @And("a merchant with name {string}, who is registered with Simple DTU Pay")
    public void aMerchantWithNameWhoIsRegisteredWithSimpleDTUPay(String name) {
        merchant = new Merchant(name);
        merchantResponseObj = dtupay.register(merchant);
        statusCode = (int) merchantResponseObj.get("status");
        merchantId = "merchant-id-" + merchant.getName();
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
        paymentResponseObj = dtupay.listPayments(merchantId);
        payments = (String) paymentResponseObj.get("payments");
        statusCode = (int) paymentResponseObj.get("status");
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", statusCode);
        if(statusCode != 200 || payments == null || payments.isEmpty()) {
            successful = false;
        } else {
            successful = true;
        }
        assertTrue(successful, "Retrieving payments for merchant "+ merchantId + " failed with status code " + statusCode);
    }

    @Then("the list contains a payments where customer {string} paid {string} kr to merchant {string}")
    public void theListContainsAPaymentsWhereCustomerPaidKrToMerchant(String customerName, String amount, String merchantName) {
        boolean paymentExists = payments.contains(customerName) && payments.contains(amount) && payments.contains(merchantName); 
        System.out.println("Verifying payment: " + customerName + " paid " + amount + " kr to " + merchantName);
        assertTrue(paymentExists, "No payment found where customer " + customerName + " paid " + amount + " kr to merchant " + merchantName);
    }

    @When("the merchant initiates a payment for {string} kr using customer id {string}")
    public void the_merchant_initiates_a_payment_for_kr_using_customer_id(String amount, String customerId) {
        // Write code here that turns the phrase above into concrete actions
        paymentResponseObj = dtupay.pay(amount, customerId, merchantId);
        statusCode = (int) paymentResponseObj.get("status");
        successful = (statusCode == 200);
    }

    @Then("the payment is not successful")
    public void the_payment_is_not_successful() {
        // Write code here that turns the phrase above into concrete actions
        assertTrue(!successful, "Payment was unexpectedly successful with status code " + statusCode);
    }
    @Then("an error message is returned saying {string}")
    public void an_error_message_is_returned_saying(String string) {
        // Write code here that turns the phrase above into concrete actions
        System.out.println("Payment response object: " + paymentResponseObj);
        String errorMessage = (String) paymentResponseObj.get("message");
        assertTrue(errorMessage.contains(string), "Expected error message to contain: " + string + " but got: " + errorMessage);
    }

    @When("the customer initiates a payment for {string} kr using merchant id {string}")
    public void the_customer_initiates_a_payment_for_kr_using_merchant_id(String amount, String merchantId) {
        // Write code here that turns the phrase above into concrete actions
        paymentResponseObj = dtupay.pay(amount, customerId, merchantId);
        statusCode = (int) paymentResponseObj.get("status");
        successful = (statusCode == 200);
    }
}