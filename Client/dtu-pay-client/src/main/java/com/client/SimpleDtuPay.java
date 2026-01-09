package com.client;

import com.client.utils.ApiCall;
import java.net.http.HttpResponse;

public class SimpleDtuPay {
    private final ApiCall apiCall;

    public SimpleDtuPay() {
        String BASE_URL = "http://localhost:8080";
        this.apiCall = new ApiCall(BASE_URL);
    }

    public String register(Customer user) {
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
    
    public boolean pay(float amount, String customerId, String merchantId) {
        try {
            String jsonBody = String.format(java.util.Locale.US, "{\"amount\":%.2f,\"customerId\":\"%s\",\"merchantId\":\"%s\"}", amount, customerId, merchantId);
            System.out.println("Payment request body: " + jsonBody);
            String res = apiCall.post("/payment/pay", jsonBody);
            System.out.println("Payment response: " + res);
            if (res.length() == 0) {
                System.err.println("Payment failed with response: " + res);
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Payment failed: " + e.getMessage());
            return false;
        }
    }

    public boolean pay(String amount, String customerId, String merchantId) {
        try {
            float amt = Float.parseFloat(amount);
            String jsonBody = String.format(java.util.Locale.US, "{\"amount\":%.2f,\"customerId\":\"%s\",\"merchantId\":\"%s\"}", amt, customerId, merchantId);
            System.out.println("Payment request body: " + jsonBody);
            String res = apiCall.post("/payment/pay", jsonBody);
            System.out.println("Payment response: " + res);
            if (res.length() == 0) {
                System.err.println("Payment failed with response: " + res);
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Payment failed: " + e.getMessage());
            return false;
        }
    }
}