package org.thebois.endpoints.services;

import jakarta.ws.rs.core.Response;
import org.thebois.DTO.CustomerDTO;
import org.thebois.utils.StorageHandler;


import dtu.ws.fastmoney.Account;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;

import java.util.HashMap;
import java.util.Map;


public class CustomerService {
    
    private final BankService bank = new BankService_Service().getBankServicePort();
    private final StorageHandler storageHandler = StorageHandler.getInstance();


    public CustomerService() {

    }

    public Response register(CustomerDTO request) {
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
                        .entity("{\"error\": \"Bank account details do not match provided customer data.\"}")
                        .build();
            }

            String serverUUID = java.util.UUID.randomUUID().toString();

            storageHandler.storeCustomer(serverUUID, account);

            if (storageHandler.getCustomer(serverUUID) == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"Failed to store customer data.\"}")
                        .build();
            } else {
                return Response.status(Response.Status.CREATED)
                        .entity(serverUUID)
                        .build();
            }


        } catch (BankServiceException_Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Bank account not found: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to register customer: " + e.getMessage() + "\"}")
                    .build();
        }

    }

    public Response getCustomer(String customerId) throws BankServiceException_Exception {
        Account customer = storageHandler.getCustomer(customerId); // Ensure customer is in storage

        if (customer == null) {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "customer with id \"" + customerId + "\" is unknown");
            return Response.status(Response.Status.NOT_FOUND)
                .entity(responseMap)
                .build();
        }

        Map<String, Object> customerMap = new HashMap<>();
        customerMap.put("customerId", customer.getId());
        customerMap.put("firstName", customer.getUser().getFirstName());
        customerMap.put("lastName", customer.getUser().getLastName());
        customerMap.put("cpr", customer.getUser().getCprNumber());
        return Response.status(Response.Status.OK)
            .entity(customerMap)
            .build();
    }
}