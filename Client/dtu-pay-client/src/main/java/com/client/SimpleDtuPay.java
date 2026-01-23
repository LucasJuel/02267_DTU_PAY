package com.client;

import com.client.utils.ApiCall;
import java.net.http.HttpResponse;
import java.util.HashMap;

import dtu.ws.fastmoney.User;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.io.StringReader;

/**
 @author BertramKjær
 **/
public class SimpleDtuPay {
    private final ApiCall apiCall;

    public SimpleDtuPay() {
        String envUrl = System.getenv("SERVER_URL");

        String BASE_URL = (envUrl != null) ? envUrl : "http://localhost:8080";

        this.apiCall = new ApiCall(BASE_URL);
    }


    public void registerCustomer(User customer, String account) {
        registerCustomerWithResponse(customer, account);
    }

    public HashMap<String, Object> registerCustomerWithResponse(User customer, String account) {
        try {
            String jsonBody = String.format(
                    "{\"firstName\":\"%s\", \"lastName\":\"%s\", \"cpr\":\"%s\", \"bankAccountId\":\"%s\"}",
                    customer.getFirstName(), customer.getLastName(), customer.getCprNumber(), account
            );

            HttpResponse<String> response = apiCall.post("/customer", jsonBody);
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", response.statusCode());
            responseMap.put("body", response.body());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                responseMap.put("dtuPayID", response.body());
            }
            return responseMap;
        } catch (Exception e) {
            HashMap<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", 500);
            return errorMap;
        }

    }

        public void registerMerchant(User merchant, String account) {
        registerMerchantWithResponse(merchant, account);
    }

    public HashMap<String, Object> registerMerchantWithResponse(User merchant, String account) {
        try {
            String jsonBody = String.format(
                    "{\"firstName\":\"%s\", \"lastName\":\"%s\", \"cpr\":\"%s\", \"bankAccountId\":\"%s\"}",
                    merchant.getFirstName(), merchant.getLastName(), merchant.getCprNumber(), account
            );

            HttpResponse<String> response = apiCall.post("/merchant", jsonBody);
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", response.statusCode());
            responseMap.put("body", response.body());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                responseMap.put("dtuPayID", response.body());
            }
            return responseMap;
        } catch (Exception e) {
            HashMap<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", 500);
            return errorMap;
        }

    }

    public HashMap<String, Object> registerPayment(float amount, String customerId, String merchantId, String description) {
        try {
            HashMap<String, Object> customer = getCustomer(customerId);
            HashMap<String, Object> merchant = getMerchant(merchantId);

   
            int statusCode = (int) customer.get("status");
            int merchantStatusCode = (int) merchant.get("status");

            if(statusCode != 200) {
                HashMap<String, Object> responseMap = new HashMap<>();
                responseMap.put("status", statusCode);
                responseMap.put("message", customer.get("customer"));
                return responseMap;
            }
            if(merchantStatusCode != 200) {
                HashMap<String, Object> responseMap = new HashMap<>();
                responseMap.put("status", merchantStatusCode);
                responseMap.put("message", merchant.get("merchant"));
                return responseMap;
            }
            String jsonBody = String.format(java.util.Locale.US, "{\"customerAccountId\":\"%s\",\"merchantAccountId\":\"%s\",\"amount\":%.2f,\"message\":\"%s\"}", customerId, merchantId, amount, description);
            HttpResponse<String> response = apiCall.post("/payment", jsonBody);
            
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

    private HashMap<String, Object> getCustomer(String customerId) {
        try {
            HttpResponse<String> response = apiCall.get("/customer/" + customerId);
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", response.statusCode());
            
            JsonObject jsonObject = Json.createReader(new StringReader(response.body())).readObject();
            
            // Check if response has "message" field (error case) or "customerId" field (success case)
            if (jsonObject.containsKey("message")) {
                responseMap.put("customer", jsonObject.getString("message"));
            } else if (jsonObject.containsKey("customerId")) {
                // Success - customer found, return the customerId
                responseMap.put("customer", jsonObject.getString("customerId"));
            }
        
            return responseMap;
        } catch (Exception e) {
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", 500);
            System.err.println("Get Customer failed: " + e.getMessage());
            return responseMap;
        }
    }
    private HashMap<String, Object> getMerchant(String merchantId) {       
        try {
            HttpResponse<String> response = apiCall.get("/merchant/" + merchantId);
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", response.statusCode());
            
            JsonObject jsonObject = Json.createReader(new StringReader(response.body())).readObject();
            
            // Check if response has "message" field (error case) or "merchantId" field (success case)
            if (jsonObject.containsKey("message")) {
                responseMap.put("merchant", jsonObject.getString("message"));
            } else if (jsonObject.containsKey("merchantId")) {
                // Success - merchant found
                responseMap.put("merchant", jsonObject.getString("merchantId"));
            }
        
            return responseMap;
        } catch (Exception e) {
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", 500);
            System.err.println("Get Merchant failed: " + e.getMessage());
            return responseMap;
        }
    }
}
