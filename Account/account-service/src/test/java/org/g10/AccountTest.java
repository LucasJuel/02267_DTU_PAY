package org.g10;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.After;
import org.g10.DTO.CustomerDTO;
import org.g10.DTO.MerchantDTO;
import org.g10.services.AccountConsumer;
import org.g10.services.AccountServiceApplication;
import org.g10.services.CustomerService;
import org.g10.services.MerchantService;

import com.rabbitmq.client.AMQP;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {
    private AccountConsumer consumer;
    private CustomerDTO customer;
    private MerchantDTO merchant;
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private AccountServiceApplication app;
    private String customer_result;
    private String merchant_result;
    private Thread thread;

    @Before
    public void setup() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare("account.customer", true, false, false, null);
        channel.queueDeclare("account.merchant", true, false, false, null);
        thread = new Thread(() -> {
            app = new AccountServiceApplication();
            AccountServiceApplication.main(new String[]{});
        });
        thread.start();
        try {
            Thread.sleep(2000); // Wait for the service to start
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Given("the account service is running")
    public void theAccountServiceIsRunning() {
        assertNotNull(app);
    }

    @When("a customer with name {string}, last name {string}, and CPR {string}")
    public void aCustomerWithNameLastNameAndCPR(String arg0, String arg1, String arg2) {
        customer = new CustomerDTO(arg0, arg1, arg2, "account-123");
    }

    @When("I register the customer with the account service")
    public void iRegisterTheCustomerWithTheAccountService() {
        //Publish to RabbitMQ to trigger account creation
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            CompletableFuture <String> future = new CompletableFuture<>();
            channel.basicConsume("account.customer", true, (consumerTag, message) -> {
                if (correlationId.equals(message.getProperties().getCorrelationId())) {
                    String response = new String(message.getBody());
                    future.complete(response);   
                }
            }, consumerTag -> {
            });
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(correlationId)
                    .replyTo("account.customer")
                    .build();
            String payload = String.format("{\"firstName\":\"%s\",\"lastName\":\"%s\",\"cpr\":\"%s\",\"bankAccountId\":\"%s\"}",
                    customer.getFirstName(), customer.getLastName(), customer.getCpr(), customer.getBankAccountId());
            channel.basicPublish("", "account.customer", props, payload.getBytes());
            customer_result = future.get(5, java.util.concurrent.TimeUnit.SECONDS); // Wait for the response
            System.out.println("Received response: " + customer_result);
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Then("there is a message in the account queue with the customer details")
    public void thereIsAMessageInTheAccountQueueWithTheCustomerDetails() {
        try {
            Thread.sleep(2000); // Wait for the message to be processed
            consumer = app.getConsumer();
            CustomerService service = consumer.getCustomerService();
            // Verify that the customer was created in the service
            CustomerDTO customer1 = service.getCustomer(customer_result);
            assertNotNull(customer1);
            assertEquals(customer.getFirstName(), customer1.getFirstName());
            assertEquals(customer.getLastName(), customer1.getLastName());
            assertEquals(customer.getCpr(), customer1.getCpr());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @When("a merchant with name {string}, last name {string}, and CPR {string}")
    public void aMerchantWithNameLastNameAndCPR(String arg0, String arg1, String arg2) {
        merchant = new MerchantDTO(arg0, arg1, arg2, "account-123");
    }

    @When("I register the merchant with the account service")
    public void iRegisterTheMerchantWithTheAccountService() {
        //Publish to RabbitMQ to trigger account creation
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            CompletableFuture <String> future = new CompletableFuture<>();
            channel.basicConsume("account.merchant", true, (consumerTag, message) -> {
                if (correlationId.equals(message.getProperties().getCorrelationId())) {
                    String response = new String(message.getBody());
                    future.complete(response);   
                }
            }, consumerTag -> {
            });
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(correlationId)
                    .replyTo("account.merchant")
                    .build();
            String payload = String.format("{\"firstName\":\"%s\",\"lastName\":\"%s\",\"cpr\":\"%s\",\"bankAccountId\":\"%s\"}",
                    merchant.getFirstName(), merchant.getLastName(), merchant.getCpr(), merchant.getBankAccountId());
            channel.basicPublish("", "account.merchant", props, payload.getBytes());
            merchant_result = future.get(5, java.util.concurrent.TimeUnit.SECONDS); // Wait for the response
            System.out.println("Received Merchant response: " + merchant_result);
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Then("there is a message in the account queue with the merchant details")
    public void thereIsAMessageInTheAccountQueueWithTheMerchantDetails() {
        try {
            Thread.sleep(2000); // Wait for the message to be processed
            consumer = app.getConsumer();
            MerchantService service = consumer.getMerchantService();
            // Verify that the merchant was created in the service
            MerchantDTO merchant1 = service.getMerchant(merchant_result);
            assertNotNull(merchant1);
            assertEquals(merchant.getFirstName(), merchant1.getFirstName());
            assertEquals(merchant.getLastName(), merchant1.getLastName());
            assertEquals(merchant.getCpr(), merchant1.getCpr());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // @After
    // public void teardown() throws IOException, TimeoutException {
    //     if (channel != null && channel.isOpen()) {
    //         channel.close();
    //     }
    //     if (connection != null && connection.isOpen()) {
    //         connection.close();
    //     }
    //     if (thread != null && thread.isAlive()) {
    //         thread.interrupt();
    //     }
    // }
}
