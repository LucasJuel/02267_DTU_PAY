package test.org.g10;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RabbitTest {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";
    private static final String QUEUE_NAME = "rabbit.test";

    private Connection connection;
    private Channel channel;

    @Given("a RabbitMQ connection")
    public void aRabbitMqConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(getEnv("RABBITMQ_HOST", DEFAULT_HOST));
        factory.setPort(getEnvInt("RABBITMQ_PORT", DEFAULT_PORT));
        factory.setUsername(getEnv("RABBITMQ_USER", DEFAULT_USERNAME));
        factory.setPassword(getEnv("RABBITMQ_PASSWORD", DEFAULT_PASSWORD));
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, true, null);
    }

    @When("I publish {string} to the rabbit test queue")
    public void iPublishToTheRabbitTestQueue(String message) throws Exception {
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Then("I can consume {string} from the rabbit test queue")
    public void iCanConsumeFromTheRabbitTestQueue(String expected) throws Exception {
        var delivery = channel.basicGet(QUEUE_NAME, true);
        assertNotNull(delivery, "Expected a message to be available on the test queue.");
        String actual = new String(delivery.getBody(), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(expected, actual);
    }

    @After
    public void cleanup() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }

    private static String getEnv(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int getEnvInt(String key, int fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
