package org.g10.services;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;
import dtu.ws.fastmoney.BankServiceException_Exception;

import org.g10.DTO.PaymentDTO;
import org.g10.utils.StorageHandler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;



public class PaymentService {
    private final BankService bank = new BankService_Service().getBankServicePort();
    private final StorageHandler storageHandler = StorageHandler.getInstance();
    public String register(PaymentDTO request) {
        try{
            // Payment processing implementation
            String merchantSimpleId = request.getMerchantAccountId();
            String customerSimpleId = request.getCustomerAccountId();

            BigDecimal amount = request.getAmount();

            if(amount.compareTo(BigDecimal.ZERO) <= 0){
                return
                    "{\"error\": \"Payment amount must be greater than zero.\"}";
            }

            String message = request.getMessage();

            if(message == null){
                message = "Payment from " + customerSimpleId + " to " + merchantSimpleId;
            }

            System.out.println("Payment details - Merchant ID: " + merchantSimpleId + ", Customer ID: " + customerSimpleId + ", Amount: " + amount);

            
            bank.transferMoneyFromTo(customerSimpleId, merchantSimpleId, amount , message);

            
            Map<String, Object> payment = new HashMap<>();
            payment.put("merchantId", merchantSimpleId);
            payment.put("customerId", customerSimpleId);
            payment.put("amount", amount);
            payment.put("message", message);
            // Should also include token.
            storageHandler.addPayment(payment);
            System.out.println("Payment recorded successfully: " + payment);
        
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Payment registered successfully");
            responseBody.put("merchantId", merchantSimpleId);
            responseBody.put("customerId", customerSimpleId);
            responseBody.put("amount", amount);
        
        
  
            return "Success!";
        } catch(BankServiceException_Exception e){
            // Bank rejected the transfer (insufficient funds, unknown account, etc.)
            String reason = e.getFaultInfo() != null ? e.getFaultInfo().getMessage() : e.getMessage();
            return "{\"error\": \"Failed to process payment: " + reason + "\"}";
        } catch(Exception e){
            e.printStackTrace();
            return "{\"error\": \"Failed to process payment: " + e.getMessage() + "\"}";
        }
    }
}
