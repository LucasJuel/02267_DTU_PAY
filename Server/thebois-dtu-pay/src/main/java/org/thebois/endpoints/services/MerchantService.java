package org.thebois.endpoints.services;

import jakarta.ws.rs.core.Response;
import org.thebois.DTO.MerchantDTO;

import dtu.ws.fastmoney.Account;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;

import java.util.HashMap;
import java.util.Map;


public class MerchantService {
    
    private final BankService bank = new BankService_Service().getBankServicePort();


    public MerchantService() {

    }

    public Response register(MerchantDTO request) {
        try {
            Account account = bank.getAccount(request.getBankAccountId());
            if (account == null || account.getUser() == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Bank account not found.\"}")
                        .build();
            }

            String cpr = account.getUser().getCprNumber();
            String firstName = account.getUser().getFirstName();
            String lastName = account.getUser().getLastName();
            if (!request.getCpr().equals(cpr)
                    || !request.getFirstName().equals(firstName)
                    || !request.getLastName().equals(lastName)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Bank account details do not match provided merchant data.\"}")
                        .build();
            }

            return Response.status(Response.Status.CREATED)
                    .entity(account.getId())
                    .build();
        } catch (BankServiceException_Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Bank account not found: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to register merchant: " + e.getMessage() + "\"}")
                    .build();
        }

    }

    public Response getMerchant(String merchantId) throws BankServiceException_Exception {
        
        try {
            Account merchant = bank.getAccount(merchantId);
            
            Map<String, Object> merchantMap = new HashMap<>();
            merchantMap.put("merchantId", merchant.getId());
            merchantMap.put("firstName", merchant.getUser().getFirstName());
            merchantMap.put("lastName", merchant.getUser().getLastName());
            merchantMap.put("cpr", merchant.getUser().getCprNumber());
            return Response.status(Response.Status.OK)
                .entity(merchantMap)
                .build();
                
        } catch (BankServiceException_Exception e) {
            // Account not found in bank
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "merchant with id " + merchantId + " is unknown");
            return Response.status(Response.Status.NOT_FOUND)
                .entity(responseMap)
                .build();
        }
    }
}