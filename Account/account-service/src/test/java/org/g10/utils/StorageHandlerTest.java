package org.g10.utils;

import org.g10.DTO.CustomerDTO;
import org.g10.DTO.MerchantDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageHandlerTest {

    private StorageHandler storage;

    @BeforeEach
    void setUp() {
        storage = StorageHandler.getInstance();
        storage.clearAll();
    }

    @Test
    void storeAndGetCustomer() {
        CustomerDTO customer = new CustomerDTO("Clara", "Clausen", "567890-1234", "account-789");
        storage.storeCustomer("cust-1", customer);

        CustomerDTO stored = storage.getCustomer("cust-1");

        assertNotNull(stored);
        assertEquals("Clara", stored.getFirstName());
        assertEquals("Clausen", stored.getLastName());
        assertEquals("567890-1234", stored.getCpr());
        assertEquals("account-789", stored.getBankAccountId());
    }

    @Test
    void storeAndGetMerchant() {
        MerchantDTO merchant = new MerchantDTO("Dan", "Dahl", "222333-4444", "account-999");
        storage.storeMerchant("merch-1", merchant);

        MerchantDTO stored = storage.getMerchant("merch-1");

        assertNotNull(stored);
        assertEquals("Dan", stored.getFirstName());
        assertEquals("Dahl", stored.getLastName());
        assertEquals("222333-4444", stored.getCpr());
        assertEquals("account-999", stored.getBankAccountId());
    }
}
