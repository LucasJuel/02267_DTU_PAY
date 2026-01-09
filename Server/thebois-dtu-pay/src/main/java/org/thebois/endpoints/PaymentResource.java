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


            Map<String, Object> payment = new HashMap<>();
            payment.put("merchantId", merchantId);
            payment.put("amount", amount);
            payment.put("customerId", customerId);
            existingPayments.add(payment);
            fileHandler.write(existingPayments);
            return Response.ok().build();
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
}


//DTO for pay
class PaymentRequest {
    private int amount;
    private String customerId;
    private String merchantId;
    private int status;

    public PaymentRequest() {
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
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

    public void setStatus(int status){
        this.status = status;
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
