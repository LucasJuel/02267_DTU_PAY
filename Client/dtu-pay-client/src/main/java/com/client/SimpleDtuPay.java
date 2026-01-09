package com.client;

public class SimpleDtuPay {
    public SimpleDtuPay() {
        // Constructor implementation
    }
    // Implementation of SimpleDtuPay class

    public String register(Customer user) {
        // Registration implementation
        return "customer-id-" + user.getName();
    }
    
    public String register(Merchant user) {
        // Registration implementation
        return "merchant-id-" + user.getName();
    }
    
    public boolean pay(int amount, String customerId, String merchantId) {
        // Payment implementation
        return true;
    }
}