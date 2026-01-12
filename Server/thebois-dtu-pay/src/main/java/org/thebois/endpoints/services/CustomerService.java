package org.thebois.endpoints.services;

import jakarta.ws.rs.core.Response;
import org.thebois.DTO.CustomerDTO;
import org.thebois.utils.FileHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomerService {

    private final FileHandler fileHandler;
    private final List<Map<String, Object>> customers;

    public CustomerService(FileHandler fileHandler, List<Map<String, Object>> customers) {
        this.fileHandler = fileHandler;
        this.customers = customers;
    }

    public Response registerCustomer(CustomerDTO request, String bankAccountId) {
        try {
            boolean exists = customers.stream()
                    .anyMatch(c -> request.getCpr().equals(String.valueOf(c.get("Cpr"))));

            if (exists) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Customer already exists!")
                        .build();
            }

            String dtuID = UUID.randomUUID().toString();

            Map<String, Object> customer = new HashMap<>();
            customer.put("dtuPayID", dtuID);
            customer.put("First name", request.getFirstName());
            customer.put("Last name", request.getLastName());
            customer.put("Cpr", request.getCpr());
            customer.put("BankAccountID", bankAccountId);

            customers.add(customer);
            fileHandler.write(customers);

            return Response.status(Response.Status.CREATED)
                    .entity(dtuID)
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Registration failed: " + e.getMessage())
                    .build();
        }
    }
}