package org.g10.services;

import jakarta.ws.rs.core.Response;
import org.g10.DTO.MerchantDTO;
import org.g10.utils.StorageHandler;

import java.util.HashMap;
import java.util.Map;


public class MerchantService {

    private final StorageHandler storageHandler = StorageHandler.getInstance();


    public MerchantService() {

    }

    public String register(MerchantDTO request) {
        try {
            String serverUUID = java.util.UUID.randomUUID().toString();

            storageHandler.storeMerchant(serverUUID, request);
            return serverUUID;
        } catch (Exception e) {
            return null;
        }
    }

    public Response getMerchant(String merchantId) {
        MerchantDTO merchant = storageHandler.getMerchant(merchantId); // Ensure merchant is in storage

        if (merchant == null) {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "merchant with id \"" + merchantId + "\" is unknown");
            return Response.status(Response.Status.NOT_FOUND)
                .entity(responseMap)
                .build();
        } else {
            return Response.status(Response.Status.OK)
                .entity(merchant)
                .build();
        }
    }

    public void handleMerchantRegistered(String message) {
        // Implementation for handling merchant registered events

    }
}