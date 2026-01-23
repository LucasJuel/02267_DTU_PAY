package org.g10.services;

import org.g10.DTO.CustomerDTO;
import org.g10.utils.StorageHandler;


public class CustomerService {

    private final StorageHandler storageHandler;


    public CustomerService() {
        this.storageHandler = StorageHandler.getInstance();
    }

    public CustomerService(StorageHandler storageHandler) {
        this.storageHandler = storageHandler;
    }

    public String register(CustomerDTO request){
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
        System.out.println("Lookup for customerId: " + customerId + ", found: " + (customer != null));
        return customer;
    }


    public String deregister(String customerId) {
        try {
            System.out.println("Attempting to deregister customer with ID: " + customerId);
            System.out.println("Current customers before deregistration: " + storageHandler.getAllCustomers().keySet());
            storageHandler.removeCustomer(customerId);
            System.out.println("Current customers after deregistration: " + storageHandler.getAllCustomers().keySet());
            return "Success!";
        } catch (Exception e) {
            return "Failure!";
        }
         
    }

}
