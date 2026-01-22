package org.g10.endpoints;

import org.g10.DTO.ReportDTO;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.g10.services.ReportingProducer;

import org.g10.DTO.CustomerDTO;
import org.g10.services.CustomerProducer;


@Path("/customer")
public class CustomerResource extends AbstractResource{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(CustomerDTO request) {
        if (request.getFirstName() == null || request.getLastName() == null
                || request.getCpr() == null || request.getBankAccountId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"firstName, lastName, cpr, and bankAccountId are required.\"}")
                    .build();
        }
        return handleRegister(request, () -> {
            try (CustomerProducer producer = new CustomerProducer()) {
                String response = producer.publishCustomerRegistered(request);
                return Response.accepted()
                        .entity(response)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"error\": \"Customer lookup is not supported via the api-gateway.\"}")
                .build();

    }

    @DELETE
    @Path("/{customerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCustomer(@jakarta.ws.rs.PathParam("customerId") String customerId) {
        System.out.println("Received request to delete customer with ID: " + customerId);
        if (customerId == null || customerId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"customerId is required.\"}")
                    .build();
        }
        return handleDeregister(customerId, () -> {
            try (CustomerProducer producer = new CustomerProducer()) {
                String response = producer.publishCustomerDeleted(customerId);
                return Response.accepted()
                        .entity(response)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
