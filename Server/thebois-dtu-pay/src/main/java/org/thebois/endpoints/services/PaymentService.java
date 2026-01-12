package org.thebois.endpoints.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.thebois.DTO.PaymentDTO;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;
import jakarta.ws.rs.core.Response;
import dtu.ws.fastmoney.Account;


public class PaymentService {
    private final BankService bank = new BankService_Service().getBankServicePort();
    public Response register(PaymentDTO request) {
        
        

        try{


            // Payment processing implementation
            String merchantId = request.getMerchantAccountId();
            String customerId = request.getCustomerAccountId();



            if(merchantId == null || customerId == null){
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Merchant ID and Customer ID must be provided.\"}")
                    .build();
            }

            Account merchantAccount = bank.getAccount(merchantId);
            Account customerAccount = bank.getAccount(customerId);  

            if(customerAccount == null){
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Customer " + customerId + " is not registered.\"}")
                    .build();
            }
            if(merchantAccount == null){
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Merchant " + merchantId + " is not registered.\"}")
                    .build();
            }

            BigDecimal amount = new BigDecimal(request.getAmount());

            if(amount.compareTo(BigDecimal.ZERO) <= 0){
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Payment amount must be greater than zero.\"}")
                    .build();
            }

            String message = request.getMessage();

            if(message == null){
                message = "Payment from " + customerId + " to " + merchantId;
            }

            System.out.println("Payment details - Merchant ID: " + merchantId + ", Customer ID: " + customerId + ", Amount: " + amount);

            Map<String, Object> payment = new HashMap<>();


            bank.transferMoneyFromTo(customerId, merchantId, amount , message);
            System.out.println("Payment recorded successfully: " + payment);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Payment registered successfully");
            responseBody.put("merchantId", merchantId);
            responseBody.put("customerId", customerId);
            responseBody.put("amount", amount);
        
        
  
            return Response.ok().entity(responseBody).build();
        } catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to process payment: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
