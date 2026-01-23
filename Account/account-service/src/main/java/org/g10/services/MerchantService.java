package org.g10.services;

import org.g10.DTO.MerchantDTO;
import org.g10.utils.StorageHandler;
/**
 @author TheZoap
 **/


public class MerchantService {

    private final StorageHandler storageHandler;


    public MerchantService() {
        storageHandler = StorageHandler.getInstance();
    }

    public MerchantService(StorageHandler storageHandler) {
        this.storageHandler = storageHandler;
    }

    public String register(MerchantDTO request){
        try {
            String serverUUID = java.util.UUID.randomUUID().toString();
            storageHandler.storeMerchant(serverUUID, request);
            return serverUUID;
        } catch (Exception e) {
            return null;
        }
    }

    public MerchantDTO getMerchant(String merchantId) {
        MerchantDTO merchant = storageHandler.getMerchant(merchantId); // Ensure merchant is in storage
        return merchant;
    }

    public String deregister(String merchantId) {
        try {
        
            storageHandler.removeMerchant(merchantId);
            return "Success!";
        } catch (Exception e) {
        
            return "Failure!";
        }
         
    }

}