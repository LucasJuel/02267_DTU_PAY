package org.thebois.endpoints;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.thebois.utils.FileHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Path("/bankCustomer")
public class BankCustomerRegistrationResource {
    private static final String STORAGE_FILE = "bank_customer.json";

    private final FileHandler fileHandler = new FileHandler(STORAGE_FILE);

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerCustomer(BankCustomerRequest request) {
        try {
            List<Map<String, Object>> customers = fileHandler.read();

            boolean exists = customers.stream().anyMatch(c -> request.getCpr().equals(c.get("cpr")));
            if (exists) {
                return Response.status(400).entity("Customer already exists!").build();
            }

            String dtuID = UUID.randomUUID().toString();

            Map<String, Object> customer = new HashMap<>();
            customer.put("dtuPayID", dtuID);
            customer.put("First name", request.getFirstName());
            customer.put("Last name", request.getLastName());
            customer.put("Cpr", request.getCpr());
            customer.put("BankAccountID", request.getBankAccountId());

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

    public static class BankCustomerRequest {
        private String firstName;
        private String lastName;
        private String cpr;
        private String bankAccountId;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getCpr() { return cpr; }
        public void setCpr(String cpr) { this.cpr = cpr; }
        public String getBankAccountId() { return bankAccountId; }
        public void setBankAccountId(String bankAccountId) { this.bankAccountId = bankAccountId; }
    }

}


