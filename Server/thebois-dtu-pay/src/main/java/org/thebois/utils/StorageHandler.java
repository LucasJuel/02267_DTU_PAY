package org.thebois.utils;

import java.util.Map;
import dtu.ws.fastmoney.Account;

public class StorageHandler {
    
    private static StorageHandler instance;
    
    private Map<String, Account> customerStorage = new java.util.HashMap<>();
    private Map<String, Account> merchantStorage = new java.util.HashMap<>();

    // Private constructor
    private StorageHandler() {}
    
    // Get singleton instance
    public static synchronized StorageHandler getInstance() {
        if (instance == null) {
            instance = new StorageHandler();
        }
        return instance;
    }

    public void storeCustomer(String customerId, Account customerData){
        customerStorage.put(customerId, customerData);
    }

    public Account getCustomer(String customerId){
        return customerStorage.get(customerId);
    }

    public void storeMerchant(String merchantId, Account merchantData){
        merchantStorage.put(merchantId, merchantData);
    }

    public Account getMerchant(String merchantId){
        return merchantStorage.get(merchantId);
    }
}