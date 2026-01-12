package com.client;

import com.client.utils.ApiCall;
import java.net.http.HttpResponse;
import java.util.HashMap;

import dtu.ws.fastmoney.User;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.io.StringReader;

public class SimpleDtuPay {
    private final ApiCall apiCall;

    public SimpleDtuPay() {
        String envUrl = System.getenv("SERVER_URL");

        String BASE_URL = (envUrl != null) ? envUrl : "http://localhost:8080";

        this.apiCall = new ApiCall(BASE_URL);
    }

    public HashMap<String, Object> register(Customer user) {
        String customerId = "customer-id-" + user.getFirstName();
        try {
            String jsonBody = String.format("{\"customerId\":\"%s\",\"name\":\"%s\"}", customerId, user.getFirstName());
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

    public HashMap<String, Object> registerUserFromBankAccount(User customer, String account) {
        try {
            String jsonBody = String.format(
                    "{\"firstName\":\"%s\", \"lastName\":\"%s\", \"cpr\":\"%s\", \"bankAccountId\":\"%s\"}",
                    customer.getFirstName(), customer.getLastName(), customer.getCprNumber(), account
            );

            HttpResponse<String> response = apiCall.post("/bankCustomer/register", jsonBody);
            System.out.println("Registration response: " + response.body());
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", response.statusCode());

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

    public HashMap<String, Object> listPayments(String merchantId) {
        try {
            HttpResponse<String> response = apiCall.get("/payment/list/" + merchantId);
            System.out.println("List Payments response: " + response.body());
            HashMap<String, Object> responseMap = new HashMap<>();
            if(response.statusCode() == 200) {
                responseMap.put("payments", response.body());
            }
            responseMap.put("status", response.statusCode());
            return responseMap;
        } catch (Exception e) {
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", 500);
            System.err.println("List Payments failed: " + e.getMessage());
            return responseMap;
        }
    }

    private HashMap<String, Object> getCustomer(String customerId) {
        try {
            HttpResponse<String> response = apiCall.get("/customer/" + customerId);
            System.out.println("Get Customer response: " + response.body());
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", response.statusCode());
            JsonObject jsonObject = Json.createReader(new StringReader(response.body())).readObject();
            String message = jsonObject.getString("message");
            responseMap.put("customer", message);
           
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
            System.out.println("Get Merchant response: " + response.body());
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", response.statusCode());
            JsonObject jsonObject = Json.createReader(new StringReader(response.body())).readObject();
            String message = jsonObject.getString("message");
            responseMap.put("merchant", message);
           
            return responseMap;
        } catch (Exception e) {
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", 500);
            System.err.println("Get Merchant failed: " + e.getMessage());
            return responseMap;
        }
    }
}