package org.g10.services;

import jakarta.ws.rs.core.Response;
import org.g10.DTO.CustomerDTO;
import org.g10.utils.StorageHandler;

import java.util.HashMap;
import java.util.Map;


public class CustomerService {

    private final StorageHandler storageHandler = StorageHandler.getInstance();


    public CustomerService() {

    }

    public String register(CustomerDTO request) {
        try {
            String key = java.util.UUID.randomUUID().toString();
            storageHandler.storeCustomer(key, request);
            return key;


        } catch (Exception e) {
            return null;
        }

    }

    public CustomerDTO getCustomer(String customerId) {
        CustomerDTO customer = storageHandler.getCustomer(customerId); // Ensure customer is in storage
        return customer;
    }

}
