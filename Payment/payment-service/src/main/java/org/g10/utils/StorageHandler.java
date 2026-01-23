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

    public synchronized void addPayment(Map<String, Object> payment) {
        payments.add(payment);
    }

    public synchronized List<Map<String, Object>> readPayments() {
        return new ArrayList<>(payments);
    }
    
    public synchronized void clear() {
        customerStorage.clear();
        merchantStorage.clear();
        payments.clear();
    }
}