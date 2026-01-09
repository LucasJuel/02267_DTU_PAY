package com.client;

import com.client.utils.ApiCall;
import java.net.http.HttpResponse;
import java.util.HashMap;


public class SimpleDtuPay {
    private final ApiCall apiCall;

    public SimpleDtuPay() {
        String BASE_URL = "http://localhost:8080";
        this.apiCall = new ApiCall(BASE_URL);
    }

    public HashMap<String, Object> register(Customer user) {
        String customerId = "customer-id-" + user.getName();
        try {
            String jsonBody = String.format("{\"customerId\":\"%s\",\"name\":\"%s\"}", customerId, user.getName());
            HttpResponse<String> response = apiCall.post("/customer/register", jsonBody);
            System.out.println("Registration response: " + response.body());
            HashMap<String, Object> responseMap = new HashMap<>();
            if(response.statusCode() == 200) {
                responseMap.put("message", "Registration successful for customer " + customerId);
            }
            responseMap.put("status", response.statusCode());
            return responseMap;
        } catch (Exception e) {
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", 500);
            System.err.println("Registration failed: " + e.getMessage());
            return responseMap;
        }
    }
    
    public HashMap<String, Object> register(Merchant user) {
        try {
            String merchantId = "merchant-id-" + user.getName();
            String jsonBody = String.format("{\"merchantId\":\"%s\",\"name\":\"%s\"}", merchantId, user.getName());
            HttpResponse<String> response = apiCall.post("/merchant/register", jsonBody);
            System.out.println("Registration response: " + response.body());
            HashMap<String, Object> responseMap = new HashMap<>();
            if(response.statusCode() == 200) {
                responseMap.put("message", "Registration successful for merchant " + merchantId);
            }
            responseMap.put("status", response.statusCode());
            return responseMap;
        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", 500);
            return responseMap;
        }
    }
    
    public HashMap<String, Object> pay(float amount, String customerId, String merchantId) {
        try {
            String jsonBody = String.format(java.util.Locale.US, "{\"amount\":%.2f,\"customerId\":\"%s\",\"merchantId\":\"%s\"}", amount, customerId, merchantId);
            System.out.println("Payment request body: " + jsonBody);
            HttpResponse<String> response = apiCall.post("/payment/pay", jsonBody);
            
            System.out.println("Payment response: " + response.body());
            HashMap<String, Object> responseMap = new HashMap<>();
            if (response.statusCode() == 200) {
                responseMap.put("message", "Payment successful for amount " + amount);
            } 
            responseMap.put("status", response.statusCode());
            return responseMap;
        } catch (Exception e) {
            System.err.println("Payment failed: " + e.getMessage());
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", 500);
            return responseMap;
        }
    }

    public HashMap<String, Object> pay(String amount, String customerId, String merchantId) {
        try {
            float amt = Float.parseFloat(amount);
            String jsonBody = String.format(java.util.Locale.US, "{\"amount\":%.2f,\"customerId\":\"%s\",\"merchantId\":\"%s\"}", amt, customerId, merchantId);
            System.out.println("Payment request body: " + jsonBody);
            HttpResponse<String> response = apiCall.post("/payment/pay", jsonBody);
            
            System.out.println("Payment response: " + response.body());
            HashMap<String, Object> responseMap = new HashMap<>();
            if (response.statusCode() == 200) {
                responseMap.put("message", "Payment successful for amount " + amt);
            } 
            responseMap.put("status", response.statusCode());
            return responseMap;
        } catch (Exception e) {
            System.err.println("Payment failed: " + e.getMessage());
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", 500);
            return responseMap;
        }
    }
}