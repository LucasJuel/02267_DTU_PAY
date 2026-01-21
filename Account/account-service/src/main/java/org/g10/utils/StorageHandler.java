package org.g10.utils;

import org.g10.DTO.CustomerDTO;
import org.g10.DTO.MerchantDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageHandler {

    private static org.g10.utils.StorageHandler instance;

    private final Map<String, CustomerDTO> customerStorage = new java.util.HashMap<>();
    private final Map<String, MerchantDTO> merchantStorage = new java.util.HashMap<>();

    // Add in-memory payments storage
    private final List<Map<String, Object>> payments = new ArrayList<>();

    // Private constructor
    private StorageHandler() {
    }

    // Get singleton instance
    public static synchronized org.g10.utils.StorageHandler getInstance() {
        if (instance == null) {
            instance = new org.g10.utils.StorageHandler();
        }
        return instance;
    }

    public void storeCustomer(String customerId, CustomerDTO customerData) {
        customerStorage.put(customerId, customerData);
    }

    public void storeMerchant(String merchantId, MerchantDTO merchantData) {
        merchantStorage.put(merchantId, merchantData);
    }

    public CustomerDTO getCustomer(String customerId) {
        return customerStorage.get(customerId);
    }


    public MerchantDTO getMerchant(String merchantId) {
        return merchantStorage.get(merchantId);
    }

    public void removeCustomer(String customerId) {
        customerStorage.remove(customerId);
    }

    public void removeMerchant(String merchantId) {
        merchantStorage.remove(merchantId);
    }

    public void clearAll() {
        customerStorage.clear();
        merchantStorage.clear();
        payments.clear();
    }
}
