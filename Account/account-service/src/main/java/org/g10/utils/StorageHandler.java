package org.g10.utils;

import org.g10.DTO.CustomerDTO;
import org.g10.DTO.MerchantDTO;

import java.util.Map;
/**
 @author BertramKjær
 **/
public class StorageHandler {

    private static org.g10.utils.StorageHandler instance;

    private final Map<String, CustomerDTO> customerStorage = new java.util.HashMap<>();
    private final Map<String, MerchantDTO> merchantStorage = new java.util.HashMap<>();

    // Private constructor
    public StorageHandler() {

    }

    // Get singleton instance
    public static synchronized org.g10.utils.StorageHandler getInstance() {
        if (instance == null) {
            instance = new org.g10.utils.StorageHandler();
        }
        return instance;
    }

    public void storeCustomer(String customerId, CustomerDTO customerData) throws Exception {
        customerStorage.put(customerId, customerData);
    }

    public void storeMerchant(String merchantId, MerchantDTO merchantData) throws Exception {
        merchantStorage.put(merchantId, merchantData);
    }

    public CustomerDTO getCustomer(String customerId) {
        return customerStorage.get(customerId);
    }

    public MerchantDTO getMerchant(String merchantId) {
        return merchantStorage.get(merchantId);
    }

    public CustomerDTO removeCustomer(String customerId) {
        return customerStorage.remove(customerId);
    }

    public MerchantDTO removeMerchant(String merchantId) {
        return merchantStorage.remove(merchantId);
    }

    public void clearAll() {
        customerStorage.clear();
        merchantStorage.clear();
    }
}
