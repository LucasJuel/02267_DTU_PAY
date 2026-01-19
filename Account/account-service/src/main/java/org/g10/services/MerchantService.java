package org.g10.services;

import org.g10.DTO.MerchantDTO;
import org.g10.utils.StorageHandler;


public class MerchantService {

    private final StorageHandler storageHandler = StorageHandler.getInstance();


    public MerchantService() {

    }

    public String register(MerchantDTO request) {
        String serverUUID = java.util.UUID.randomUUID().toString();
        storageHandler.storeMerchant(serverUUID, request);
        return serverUUID;
    }

    public MerchantDTO getMerchant(String merchantId) {
        MerchantDTO merchant = storageHandler.getMerchant(merchantId); // Ensure merchant is in storage
        return merchant;
    }

    public void handleMerchantRegistered(String message) {
        // Implementation for handling merchant registered events

    }
}