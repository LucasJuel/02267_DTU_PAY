package org.thebois.endpoints;

import org.thebois.utils.FileHandler;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; 

@Path("/payment")
public class PaymentResource {

    FileHandler fileHandler = new FileHandler("payments.json");
    // Implementation of PaymentResource class

    @POST
    @Path("/pay")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pay(PaymentRequest paymentDetails) {
        System.out.println("Processing payment for merchant: " + paymentDetails);
        try{
            List<Map<String, Object>> existingPayments = fileHandler.read();
    
            // Payment processing implementation
            String merchantId = paymentDetails.getMerchantId();
            String customerId = paymentDetails.getCustomerId();
            float amount = paymentDetails.getAmount();
            System.out.println("Payment details - Merchant ID: " + merchantId + ", Customer ID: " + customerId + ", Amount: " + amount);


            Map<String, Object> payment = new HashMap<>();
            payment.put("merchantId", merchantId);
            payment.put("amount", amount);
            payment.put("customerId", customerId);
            existingPayments.add(payment);
            fileHandler.write(existingPayments);
            System.out.println("Payment recorded successfully: " + payment);
            System.out.println("All payments: " + existingPayments);
            return Response.ok().entity("{\"message\": \"Payment for merchant " + merchantId + " registered successfully. With Customer " + customerId + " for amount " + amount + "\", \"merchantId\": \"" + merchantId + "\", \"customerId\": \"" + customerId + "\", \"amount\": " + amount + "}").build();
        } catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to process payment: " + e.getMessage() + "\"}")
                    .build();
        }
       
    }

    @GET
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPayments(PaymentListRequest paymentListRequest){
        try{
            // List payments implementation
            List<Map<String, Object>> payments = fileHandler.read();
            List<Map<String,Object>> merchantPayments = payments.stream()
                .filter(s -> paymentListRequest.getMerchantId().equals(s.get("merchantId")))
                .collect(Collectors.toList());

            return Response.ok()
                    .entity(merchantPayments)
                    .build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to process list of payments: " + e.getMessage() + "\"}")
                    .build();
        }

    }
    public static class PaymentRequest {
        private float amount;
        private String customerId;
        private String merchantId;
        public PaymentRequest() {
        }

        public float getAmount() {
            return amount;
        }

        public void setAmount(float amount) {
            this.amount = amount;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }
    }

    //DTO for list for payments
    class PaymentListRequest {
        private String merchantId;
        public PaymentListRequest() {
        }
        public String getMerchantId() {
            return merchantId;
        }
        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }
    }
}   




