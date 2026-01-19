package org.g10;
import dtu.ws.fastmoney.*;
import io.cucumber.java.After;
import io.cucumber.java.PendingException;
import org.g10.DTO.CustomerDTO;
import org.g10.DTO.MerchantDTO;
import org.g10.DTO.PaymentDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.g10.services.PaymentConsumer;
import org.g10.services.PaymentServiceApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PaymentConsumerSteps {
    private PaymentConsumer consumer;
    private PaymentDTO paymentDTO = new PaymentDTO();
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private PaymentServiceApplication app;
    private String payment_result;
    private Thread thread;
    private final BankService bank = new BankService_Service().getBankServicePort();
    private final String apiKey = "kayak2098";
    private final User customerUser = new User();
    private final User merchantUser = new User();
    private String customerAccount;
    private String merchantAccount;
    private final List<String> accounts = new ArrayList<>();

    @Before
    public void setup() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare("payment.requests", true, false, false, null);
        
        thread = new Thread(() -> {
            app = new PaymentServiceApplication();
            PaymentServiceApplication.main(new String[]{});
        });
        thread.start();
        try {
            Thread.sleep(2000); // Wait for the service to start
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Given("the payment service is running")
    public void a_fresh_payment_store() {
        // Storage already reset in @Before
        assertNotNull(app);

    }

    @And("the customer firstname {string} lastname {string} with cpr {string} is registered with the bank with an initial balance of {int} kr")
    public void theCustomerFirstnameLastnameWithCprIsRegisteredWithTheBankWithAnInitialBalanceOfKr(String arg0, String arg1, String arg2, int initBalance) throws BankServiceException_Exception {

        customerUser.setFirstName(arg0);
        customerUser.setLastName(arg1);
        customerUser.setCprNumber(arg2);

        System.out.println("Trying to create bank account for customer: " + customerUser.getFirstName() + " " + customerUser.getLastName() + ", CPR: " + customerUser.getCprNumber());
        customerAccount = bank.createAccountWithBalance(apiKey, customerUser, new BigDecimal(initBalance));
        System.out.println("Created customer bank account: " + customerAccount);
        accounts.add(customerAccount);
        assert (bank.getAccount(customerAccount).getBalance().equals(new BigDecimal(initBalance)));
    }
    @And("the merchant firstname {string} lastname {string} with cpr {string} is registered with the bank with an initial balance of {int} kr")
    public void theMerchantFirstnameLastnameWithCprIsRegisteredWithTheBankWithAnInitialBalanceOfKr(String arg0, String arg1, String arg2, int initBalance) throws BankServiceException_Exception {

        merchantUser.setFirstName(arg0);
        merchantUser.setLastName(arg1);
        merchantUser.setCprNumber(arg2);

        BigDecimal balance = new BigDecimal(initBalance);
        System.out.println("Trying to create bank account for merchant: " + merchantUser.getFirstName() + " " + merchantUser.getLastName() + ", CPR: " + merchantUser.getCprNumber());
        merchantAccount = bank.createAccountWithBalance(apiKey, merchantUser, balance);
        System.out.println("Created merchant bank account: " + merchantAccount);

        accounts.add(merchantAccount);

        assert (Objects.equals(bank.getAccount(merchantAccount).getBalance(), balance));
    }

    @Given("a transaction between the customer and the merchant is initiated with amount {double} kr and message {string}")
    public void aTransactionBetweenTheCustomerAndTheMerchantIsInitiatedWithAmountKr(Double arg0, String message) {
        BigDecimal val = new BigDecimal(arg0);
        paymentDTO.setAmount(val);
        paymentDTO.setCustomerAccountId(customerAccount);
        paymentDTO.setMerchantAccountId(merchantAccount);
        paymentDTO.setMessage(message);
    }



    @When("I register the payment with the payment service") 
    public void i_register_the_payment_with_the_payment_service() {
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            String replyQueue = channel.queueDeclare("", false, true, true, null).getQueue();
            CompletableFuture <String> future = new CompletableFuture<>();
            String consumerTag = channel.basicConsume(replyQueue, true, (tag, message) -> {
                if (correlationId.equals(message.getProperties().getCorrelationId())) {
                    future.complete(new String(message.getBody()));
                }
                }, tag -> {
                });
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueue)
                    .build();
            String payload = new Gson().toJson(paymentDTO);
            System.out.println("payload: " + payload);
            channel.basicPublish("", "payment.requests", props, payload.getBytes());
            payment_result = future.get(5, java.util.concurrent.TimeUnit.SECONDS); // Wait for the response
            System.out.println("Received response: " + payment_result);
            channel.basicCancel(consumerTag);

        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Then("the payment service should respond with a success message")
    public void the_payment_service_should_respond_with_a_success_message() {
        System.out.println("Starting test 4");
        assertNotNull(payment_result);
        System.out.println("Payment service response: " + payment_result);
        // Further assertions can be made based on the expected response format
    }

    @And("The customer has balance {double} on their bank account and merchant has balance {double}")
    public void theCustomerHasBalanceDoubleOnTheirBankAccountAndMerchantHasBalanceDouble(double customerAmount, double merchantAmount) throws BankServiceException_Exception {
        BigDecimal customerBalance = bank.getAccount(customerAccount).getBalance();
        BigDecimal merchantBalance = bank.getAccount(merchantAccount).getBalance();

        System.out.println("Customer actual balance is: " + customerBalance);
        System.out.println("Customer expected balance is: " + customerAmount);
        System.out.println("Merchant actual balance is: " + merchantBalance);
        System.out.println("Merchant expected balance is: " + merchantBalance);

        assertEquals(customerBalance, new BigDecimal(customerAmount));
        assertEquals(merchantBalance, new BigDecimal(merchantAmount));

    }


    @After
    public void deleteAccounts() throws BankServiceException_Exception {
        for (String account : accounts) {
            bank.retireAccount(apiKey, account);
        }
    }
}
