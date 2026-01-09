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

@Path("/customer/register")
public class CustomerRegistrationResource {
    
    private static final String CUSTOMERS_FILE = "customers.json";
    private FileHandler fileHandler = new FileHandler(CUSTOMERS_FILE);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerCustomer(CustomerRequest customerRequest) {
        try {
            String customerId = customerRequest.getCustomerId();
            String customerName = customerRequest.getName();
            
            // Read existing customers
            List<Map<String, Object>> customers = fileHandler.read();
            
            // Check if customer already exists
            boolean exists = customers.stream()
                    .anyMatch(c -> customerId.equals(c.get("customerId")));
            
            if (exists) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Customer " + customerId + " is already registered.\"}")
                        .build();
            }
            
            // Create new customer entry
            Map<String, Object> newCustomer = new HashMap<>();
            newCustomer.put("customerId", customerId);
            newCustomer.put("name", customerName);
            newCustomer.put("registeredAt", LocalDateTime.now().toString());
            
            // Add to list
            customers.add(newCustomer);
            
            // Save to file
            fileHandler.write(customers);
            
            return Response.ok()
                    .entity("{\"message\": \"Customer " + customerId + " registered successfully.\", \"customerId\": \"" + customerId + "\"}")
                    .build();
                    
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to register customer: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    // DTO class for customer registration request
    public static class CustomerRequest {
        private String customerId;
        private String name;
        
        public CustomerRequest() {
        }
        
        public String getCustomerId() {
            return customerId;
        }
        
        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
}
