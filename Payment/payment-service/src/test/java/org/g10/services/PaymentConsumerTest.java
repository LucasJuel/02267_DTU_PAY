package test.java.org.g10.services;

import org.g10.utils.StorageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentConsumerTest {

    private StorageHandler storage;

    @BeforeEach
    void setUp() throws Exception {
        storage = resetStorageHandlerSingleton();
    }

    @Test
    void handlePaymentRequestedPersistsPaymentInStorage() {
        PaymentConsumer consumer = new PaymentConsumer(storage);

        String payload = """
                {
                  "customerAccountId": "cust-42",
                  "merchantAccountId": "merch-99",
                  "amount": 125.75,
                  "message": "Paying for coffee"
                }
                """;

        consumer.handlePaymentRequested(payload);

        List<Map<String, Object>> payments = storage.readPayments();
        assertEquals(1, payments.size(), "Payment should be recorded once");
        Map<String, Object> storedPayment = payments.get(0);

        assertEquals("cust-42", storedPayment.get("customerAccountId"));
        assertEquals("merch-99", storedPayment.get("merchantAccountId"));
        assertEquals(125.75, ((Number) storedPayment.get("amount")).doubleValue());
        assertEquals("Paying for coffee", storedPayment.get("message"));
    }

    private StorageHandler resetStorageHandlerSingleton() throws Exception {
        Field instanceField = StorageHandler.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        return StorageHandler.getInstance();
    }
}
