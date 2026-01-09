package org.thebois.endpoints;

import org.thebois.utils.FileHandler;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/merchant")
public class MerchantRegistrationResource {
    
    private static final String MERCHANTS_FILE = "merchants.json";
    private FileHandler fileHandler = new FileHandler(MERCHANTS_FILE);

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerMerchant(MerchantRequest merchantRequest) {
        try {
            String merchantId = merchantRequest.getMerchantId();
            String merchantName = merchantRequest.getName();
            
            // Read existing merchants
            List<Map<String, Object>> merchants = fileHandler.read();
            
            // Check if merchant already exists
            boolean exists = merchants.stream()
                    .anyMatch(c -> merchantId.equals(c.get("merchantId")));
            
            if (exists) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Merchant " + merchantId + " is already registered.\"}")
                        .build();
            }
            
            // Create new merchant entry
            Map<String, Object> newMerchant = new HashMap<>();
            newMerchant.put("merchantId", merchantId);
            newMerchant.put("name", merchantName);
            newMerchant.put("registeredAt", LocalDateTime.now().toString());
            
            // Add to list
            merchants.add(newMerchant);
            
            // Save to file
            fileHandler.write(merchants);
            
            return Response.ok()
                    .entity("{\"message\": \"Merchant " + merchantId + " registered successfully.\", \"merchantId\": \"" + merchantId + "\"}")
                    .build();
                    
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to register merchant: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{merchantId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMerchant(@jakarta.ws.rs.PathParam("merchantId") String merchantId) {
        try {
            // Read existing merchants
            List<Map<String, Object>> merchants = fileHandler.read();
            
            // Find merchant
            Map<String, Object> merchant = merchants.stream()
                    .filter(c -> merchantId.equals(c.get("merchantId")))
                    .findFirst()
                    .orElse(null);
            
            if (merchant == null) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("message", "merchant with id \"" + merchantId + "\" is unknown");
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(responseMap)
                        .build();
            }
            
            Map<String, Object> responseMap = new HashMap<>();
        
            responseMap.put("message", "\"" + merchantId + "\"");
        
            return Response.ok()
                    .entity(responseMap)
                    .build();
                    
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to retrieve customer: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    // DTO class for merchant registration request
    public static class MerchantRequest {
        private String merchantId;
        private String name;
        
        public MerchantRequest() {
        }
        
        public String getMerchantId() {
            return merchantId;
        }
        
        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
}
