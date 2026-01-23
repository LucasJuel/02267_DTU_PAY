package org.g10.utils;

import dtu.ws.fastmoney.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
@author ssschoubye
 **/
public class StorageHandler {
    
    private static StorageHandler instance;
    
    private final Map<String, Account> customerStorage = new java.util.HashMap<>();
    private final Map<String, Account> merchantStorage = new java.util.HashMap<>();

    // Add in-memory payments storage
    private final List<Map<String, Object>> payments = new ArrayList<>();

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

    public synchronized void addPayment(Map<String, Object> payment) {
        System.out.println("Adding payment: " + payment);
        payments.add(payment);
    }

    public synchronized List<Map<String, Object>> readPayments() {
        return new ArrayList<>(payments);
    }

    public synchronized List<Map<String, Object>> getPaymentsByMerchant(String merchantId) {
        return payments.stream()
                .filter(p -> merchantId.equals(p.get("merchantId")))
                .collect(Collectors.toList());
    }
    
    public synchronized void clear() {
        customerStorage.clear();
        merchantStorage.clear();
        payments.clear();
    }
}