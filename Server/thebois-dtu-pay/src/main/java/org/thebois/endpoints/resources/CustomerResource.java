package org.thebois.endpoints.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.thebois.DTO.CustomerDTO;
import org.thebois.endpoints.services.CustomerService;


@Path("/customer")
public class CustomerResource {
    private final CustomerService customerService = new CustomerService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    public Response register(CustomerDTO request) {
        System.out.println("Received registration request for customer: " + request.getFirstName() + " " + request.getLastName() + ", CPR: " + request.getCpr() + ", Bank Account: " + request.getBankAccountId());
        if (request == null || request.getFirstName() == null || request.getLastName() == null
                || request.getCpr() == null || request.getBankAccountId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"firstName, lastName, cpr, and bankAccountId are required.\"}")
                    .build();
        }
        try {
            return customerService.register(request);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"An unexpected error occurred: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{customerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomer(@jakarta.ws.rs.PathParam("customerId") String customerId) {
        System.out.println("Received request to get customer with ID: " + customerId);
        if (customerId == null || customerId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"customerId is required.\"}")
                    .build();
        }
        try {
            return customerService.getCustomer(customerId);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"An unexpected error occurred: " + e.getMessage() + "\"}")
                    .build();
        }

    }

}
