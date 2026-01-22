package org.g10;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.g10.DTO.CustomerDTO;
import org.g10.DTO.MerchantDTO;
import org.g10.services.AccountConsumer;
import org.g10.services.AccountServiceApplication;
import org.g10.services.MerchantService;
import org.g10.services.CustomerService;
import com.rabbitmq.client.AMQP;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    private Map<String, MerchantDTO> merchants = new HashMap<>();
    private Map<String, String> merchantResults = new HashMap<>();
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
        channel.queueDeclare("account.customer.deregister", true, false, false, null);
        channel.queueDeclare("account.merchant.deregister", true, false, false, null);
        if(app == null){
            thread = new Thread(() -> {
                app = new AccountServiceApplication();
                AccountServiceApplication.main(new String[]{});
            });
            thread.start();
        }
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

    @Given("a customer with name {string}, last name {string}, and CPR {string}")
    public void aCustomerWithNameLastNameAndCPR(String arg0, String arg1, String arg2) {
        customer = new CustomerDTO(arg0, arg1, arg2, "account-123");
    }

    @When("I register the customer with the account service")
    public void iRegisterTheCustomerWithTheAccountService() {
        //Publish to RabbitMQ to trigger account creation
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            String replyQueue = channel.queueDeclare("", false, true, true, null).getQueue();
            CompletableFuture<String> future = new CompletableFuture<>();
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
            String payload = String.format("{\"firstName\":\"%s\",\"lastName\":\"%s\",\"cpr\":\"%s\",\"bankAccountId\":\"%s\"}",
                    customer.getFirstName(), customer.getLastName(), customer.getCpr(), customer.getBankAccountId());
            channel.basicPublish("", "account.customer", props, payload.getBytes());
            customer_result = future.get(5, java.util.concurrent.TimeUnit.SECONDS); // Wait for the response
            System.out.println("Received response: " + customer_result);
            channel.basicCancel(consumerTag);
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException |
                 java.util.concurrent.TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Then("there is a message in the account queue with the customer details")
    public void thereIsAMessageInTheAccountQueueWithTheCustomerDetails() {
        try {
            System.out.println("Verifying customer with ID: " + customer_result);
            // Thread.sleep(2000); // Wait for the message to be processed
            // consumer = app.getConsumer();
            // CustomerService service = consumer.getCustomerService();
            // // Verify that the customer was created in the service
            // CustomerDTO customer1 = service.getCustomer(customer_result);
            // assertNotNull(customer1);
            // assertEquals(customer.getFirstName(), customer1.getFirstName());
            // assertEquals(customer.getLastName(), customer1.getLastName());
            // assertEquals(customer.getCpr(), customer1.getCpr());
            throw new UnsupportedOperationException("Not implemented yet");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Given("a merchant with name {string}, last name {string}, and CPR {string}")
    public void aMerchantWithNameLastNameAndCPR(String arg0, String arg1, String arg2) {
        merchant = new MerchantDTO(arg0, arg1, arg2, "account-123");
        merchants.put(arg0, merchant);
    }

    @When("I register the merchant with the account service")
    public void iRegisterTheMerchantWithTheAccountService() {
        //Publish to RabbitMQ to trigger account creation
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            String replyQueue = channel.queueDeclare("", false, true, true, null).getQueue();
            CompletableFuture<String> future = new CompletableFuture<>();
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
            String payload = String.format("{\"firstName\":\"%s\",\"lastName\":\"%s\",\"cpr\":\"%s\",\"bankAccountId\":\"%s\"}",
                    merchant.getFirstName(), merchant.getLastName(), merchant.getCpr(), merchant.getBankAccountId());
            channel.basicPublish("", "account.merchant", props, payload.getBytes());
            merchant_result = future.get(5, java.util.concurrent.TimeUnit.SECONDS); // Wait for the response
            System.out.println("Received Merchant response: " + merchant_result);
            channel.basicCancel(consumerTag);
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException |
                 java.util.concurrent.TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Then("there is a message in the account queue with the merchant details")
    public void thereIsAMessageInTheAccountQueueWithTheMerchantDetails() {
        try {
            System.out.println("Verifying merchant with ID: " + merchant_result);
            // Thread.sleep(2000); // Wait for the message to be processed
            // consumer = app.getConsumer();
            // MerchantService service = consumer.getMerchantService();
            // // Verify that the merchant was created in the service
            // MerchantDTO merchant1 = service.getMerchant(merchant_result);
            // assertNotNull(merchant1);
            // assertEquals(merchant.getFirstName(), merchant1.getFirstName());
            // assertEquals(merchant.getLastName(), merchant1.getLastName());
            // assertEquals(merchant.getCpr(), merchant1.getCpr());
            throw new UnsupportedOperationException("Not implemented yet");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @When("I register the merchants with the account service")
    public void iRegisterTheMerchantsWithTheAccountService() {
        // Register all merchants in the map
        for (Map.Entry<String, MerchantDTO> entry : merchants.entrySet()) {
            String merchantName = entry.getKey();
            MerchantDTO merchantDTO = entry.getValue();
            try {
                String correlationId = java.util.UUID.randomUUID().toString();
                String replyQueue = channel.queueDeclare("", false, true, true, null).getQueue();
                CompletableFuture<String> future = new CompletableFuture<>();
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
                String payload = String.format("{\"firstName\":\"%s\",\"lastName\":\"%s\",\"cpr\":\"%s\",\"bankAccountId\":\"%s\"}",
                        merchantDTO.getFirstName(), merchantDTO.getLastName(), merchantDTO.getCpr(), merchantDTO.getBankAccountId());
                channel.basicPublish("", "account.merchant", props, payload.getBytes());
                String result = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
                merchantResults.put(merchantName, result);
                System.out.println("Registered merchant " + merchantName + ": " + result);
                channel.basicCancel(consumerTag);
            } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException |
                     java.util.concurrent.TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    @Then("there is a message in the account queue for {string} with the merchant details")
    public void thereIsAMessageInTheAccountQueueForWithTheMerchantDetails(String merchantName) {
        try {
            Thread.sleep(2000); // Wait for the message to be processed
            consumer = app.getConsumer();
            MerchantService service = consumer.getMerchantService();

            // Get the result ID for this specific merchant
            String merchantId = merchantResults.get(merchantName);
            assertNotNull(merchantId, "No result found for merchant: " + merchantName);

            // Verify that the merchant was created in the service
            MerchantDTO expectedMerchant = merchants.get(merchantName);
            MerchantDTO actualMerchant = service.getMerchant(merchantId);

            assertNotNull(actualMerchant);
            assertEquals(expectedMerchant.getFirstName(), actualMerchant.getFirstName());
            assertEquals(expectedMerchant.getLastName(), actualMerchant.getLastName());
            assertEquals(expectedMerchant.getCpr(), actualMerchant.getCpr());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @When("I deregister the customer with the customer id with the account service")
    public void iDeregisterTheCustomerWithTheCustomerIdWithTheAccountService() {
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            String replyQueue = channel.queueDeclare("", false, true, true, null).getQueue();
            CompletableFuture<String> future = new CompletableFuture<>();
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
            String payload = String.format("{\"customerId\":\"%s\"}", customer_result);
            channel.basicPublish("", "account.customer.deregister", props, payload.getBytes());
            String response = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
            System.out.println("Received deregister customer response: " + response);
            channel.basicCancel(consumerTag);
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Then("there is a deregistration message in the account queue with the customer details")
    public void thereIsADeregistrationMessageInTheAccountQueueWithTheCustomerDetails() {
        try {
            Thread.sleep(2000); // Wait for the message to be processed
            consumer = app.getConsumer();
            CustomerService service = consumer.getCustomerService();
            // Verify that the customer was removed from the service
            //System.out.println(customer_result);
            CustomerDTO customer1 = service.getCustomer(customer_result);
            System.out.println("Customer after deregistration: " + customer1);
            assertNull(customer1, "Customer should be null after deregistration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @When("I deregister the merchant with the merchant id with the account service")
    public void iDeregisterTheMerchantWithTheMerchantIdWithTheAccountService() {
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            String replyQueue = channel.queueDeclare("", false, true, true, null).getQueue();
            CompletableFuture<String> future = new CompletableFuture<>();
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
            String payload = String.format("{\"merchantId\":\"%s\"}", merchant_result);
            channel.basicPublish("", "account.merchant.deregister", props, payload.getBytes());
            String response = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
            System.out.println("Received deregister merchant response: " + response);
            channel.basicCancel(consumerTag);
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Then("there is a deregistration message in the account queue with the merchant details")
    public void thereIsADeregistrationMessageInTheAccountQueueWithTheMerchantDetails() {
        try {
            Thread.sleep(2000); // Wait for the message to be processed
            consumer = app.getConsumer();
            MerchantService service = consumer.getMerchantService();
            // Verify that the merchant was removed from the service
            MerchantDTO merchant1 = service.getMerchant(merchant_result);
            System.out.println("Merchant after deregistration: " + merchant1);
            assertNull(merchant1, "Merchant should be null after deregistration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @When("I attempt to deregister a customer with id {string} with the account service")
    public void iAttemptToDeregisterACustomerWithIdWithTheAccountService(String customerId) {
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            String replyQueue = channel.queueDeclare("", false, true, true, null).getQueue();
            CompletableFuture<String> future = new CompletableFuture<>();
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
            String payload = String.format("{\"customerId\":\"%s\"}", customerId);
            channel.basicPublish("", "account.customer.deregister", props, payload.getBytes());
            String response = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
            System.out.println("Received deregister non-existent customer response: " + response);
            channel.basicCancel(consumerTag);
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            e.printStackTrace();
        }
    }

    @When("I attempt to deregister a merchant with id {string} with the account service")
    public void iAttemptToDeregisterAMerchantWithIdWithTheAccountService(String merchantId) {
        try {
            String correlationId = java.util.UUID.randomUUID().toString();
            String replyQueue = channel.queueDeclare("", false, true, true, null).getQueue();
            CompletableFuture<String> future = new CompletableFuture<>();
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
            String payload = String.format("{\"merchantId\":\"%s\"}", merchantId);
            channel.basicPublish("", "account.merchant.deregister", props, payload.getBytes());
            String response = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
            System.out.println("Received deregister non-existent merchant response: " + response);
            channel.basicCancel(consumerTag);
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
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
