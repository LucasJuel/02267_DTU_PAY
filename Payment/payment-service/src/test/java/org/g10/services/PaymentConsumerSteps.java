package test.java.org.g10.services;
import org.g10.services.PaymentConsumer;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.g10.utils.StorageHandler;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentConsumerSteps {

    private StorageHandler storage;
    private PaymentConsumer consumer;
    private List<Map<String, Object>> observedPayments = Collections.emptyList();

    @Before
    public void setUp() throws Exception {
        storage = resetStorageHandlerSingleton();
        consumer = new PaymentConsumer(storage);
        observedPayments = Collections.emptyList();
    }

    @Given("a fresh payment store")
    public void a_fresh_payment_store() {
        // Storage already reset in @Before
    }

    @When("the payment consumer handles payload:")
    public void the_payment_consumer_handles_payload(String payload) {
        consumer.handlePaymentRequested(payload);
        observedPayments = storage.readPayments();
    }

    @Then("the payment storage contains the following entry:")
    public void the_payment_storage_contains_the_following_entry(DataTable expectedTable) {
        Map<String, String> expected = expectedTable.asMap(String.class, String.class);
        assertEquals(1, observedPayments.size(), "Expected exactly one payment stored");

        Map<String, Object> actual = observedPayments.get(0);
        expected.forEach((field, expectedValue) -> {
            if ("amount".equals(field)) {
                Object actualAmountValue = actual.get(field);
                double actualAmount = actualAmountValue == null ? 0.0 : ((Number) actualAmountValue).doubleValue();
                double expectedAmount = Double.parseDouble(expectedValue);
                assertEquals(expectedAmount, actualAmount, 0.0001, "Mismatch for amount");
            } else {
                Object actualValue = actual.get(field);
                String actualAsString = actualValue == null ? null : actualValue.toString();
                assertEquals(expectedValue, actualAsString, "Mismatch for field " + field);
            }
        });
    }

    private StorageHandler resetStorageHandlerSingleton() throws Exception {
        Field instanceField = StorageHandler.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        return StorageHandler.getInstance();
    }
}
