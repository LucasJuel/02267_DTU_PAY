package org.g10;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.g10.DTO.CustomerDTO;
import org.g10.services.AccountConsumer;
import org.g10.services.AccountServiceApplication;
import org.g10.services.CustomerService;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {
    private AccountConsumer consumer;
    private CustomerDTO customer;
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private AccountServiceApplication app;

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
    }

    @Given("the account service is running")
    public void the() {
        Thread thread = new Thread(() -> {
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

    @When("a customer with name {string}, last name {string}, and CPR {string}")
    public void aCustomerWithNameLastNameAndCPR(String arg0, String arg1, String arg2) {
        customer = new CustomerDTO(arg0, arg1, arg2, "account-123");
    }

    @When("I register the customer with the account service")
    public void iRegisterTheCustomerWithTheAccountService() {
        //Publish to RabbitMQ to trigger account creation
        try {
            String payload = String.format("{\"firstName\":\"%s\",\"lastName\":\"%s\",\"cpr\":\"%s\",\"bankAccountId\":\"%s\"}",
                    customer.getFirstName(), customer.getLastName(), customer.getCpr(), customer.getBankAccountId());
            channel.basicPublish("", "account.customer", null, payload.getBytes());
        } catch (IOException e) {
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
            org.g10.DTO.CustomerDTO customer1 = service.getCustomer(customer.getAccountId());
            assertNotNull(customer1);
            assertEquals(customer.getFirstName(), customer1.getFirstName());
            assertEquals(customer.getLastName(), customer1.getLastName());
            assertEquals(customer.getCpr(), customer1.getCpr());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
