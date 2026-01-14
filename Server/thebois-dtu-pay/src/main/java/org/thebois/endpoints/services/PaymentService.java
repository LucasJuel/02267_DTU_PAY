package org.thebois.endpoints.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.thebois.DTO.PaymentDTO;
import org.thebois.utils.StorageHandler;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;
import jakarta.ws.rs.core.Response;
import dtu.ws.fastmoney.Account;



public class PaymentService {
    private final BankService bank = new BankService_Service().getBankServicePort();
    private final StorageHandler storageHandler = StorageHandler.getInstance();
    public Response register(PaymentDTO request) {
        try{
            // Payment processing implementation
            String merchantSimpleId = request.getMerchantAccountId();
            String customerSimpleId = request.getCustomerAccountId();

            Account merchantAccountStored = storageHandler.getMerchant(merchantSimpleId);
            Account customerAccountStored = storageHandler.getCustomer(customerSimpleId);

            if(customerAccountStored == null){
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Customer " + customerSimpleId + " is not registered in simple DTU pay.\"}")
                    .build();
            }
            if(merchantAccountStored == null){
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Merchant " + merchantSimpleId + " is not registered in simple DTU pay.\"}")
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
                message = "Payment from " + customerAccountStored.getId() + " to " + merchantAccountStored.getId();
            }

            System.out.println("Payment details - Merchant ID: " + merchantAccountStored.getId() + ", Customer ID: " + customerAccountStored.getId() + ", Amount: " + amount);

            Map<String, Object> payment = new HashMap<>();


            bank.transferMoneyFromTo(customerAccountStored.getId(), merchantAccountStored.getId(), amount , message);
            System.out.println("Payment recorded successfully: " + payment);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Payment registered successfully");
            responseBody.put("merchantId", merchantAccountStored.getId());
            responseBody.put("customerId", customerAccountStored.getId());
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
