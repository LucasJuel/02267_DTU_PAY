package org.g10.services;

import org.g10.DTO.CustomerDTO;
import org.g10.utils.StorageHandler;
/**
 @author ssschoubye
 **/


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
       
        return customer;
    }


    public String deregister(String customerId) {
        try {
            
            storageHandler.removeCustomer(customerId);

            return "Success!";
        } catch (Exception e) {
            return "Failure!";
        }
         
    }

}
