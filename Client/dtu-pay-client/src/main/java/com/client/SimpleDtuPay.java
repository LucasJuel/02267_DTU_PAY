package com.client;

import com.client.utils.ApiCall;

public class SimpleDtuPay {
    private final ApiCall apiCall;

    public SimpleDtuPay() {
        String BASE_URL = "http://localhost:8080";
        this.apiCall = new ApiCall(BASE_URL);
        // Constructor implementation
    }
    // Implementation of SimpleDtuPay class

    public String register(Customer user) {
        // Registration implementation
        String customerId = "customer-id-" + user.getName();
        try {
            String jsonBody = String.format("{\"customerId\":\"%s\",\"name\":\"%s\"}", customerId, user.getName());
            String res = apiCall.post("/customer/register", jsonBody);
            System.out.println("Registration response: " + res);
            return customerId;
        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
            return null;
        }
    }
    
    public String register(Merchant user) {
        // Registration implementation
        try {
            String merchantId = "merchant-id-" + user.getName();
            String jsonBody = String.format("{\"merchantId\":\"%s\",\"name\":\"%s\"}", merchantId, user.getName());
            String res = apiCall.post("/merchant/register", jsonBody);
            System.out.println("Registration response: " + res);
            return merchantId;
        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
            return null;
        }
    }
    
    public boolean pay(int amount, String customerId, String merchantId) {
        // Payment implementation
        try {
            String jsonBody = String.format("{\"amount\":%d,\"customerId\":\"%s\",\"merchantId\":\"%s\"}", amount, customerId, merchantId);
            System.out.println("Payment request body: " + jsonBody);
            String res = apiCall.post("/payment/pay", jsonBody);
            System.out.println("Payment response: " + res);
        } catch (Exception e) {
            System.err.println("Payment failed: " + e.getMessage());
            return false;
        }
        return true;
    }
}