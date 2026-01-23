package org.g10.endpoints;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.g10.services.MerchantProducer;
import org.g10.DTO.MerchantDTO;

/**
 @author BertramKjær
 **/
@Path("/merchant")
public class    MerchantResource extends AbstractResource {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(MerchantDTO request) {
        if (request == null || request.getFirstName() == null || request.getLastName() == null
                || request.getCpr() == null || request.getBankAccountId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"firstName, lastName, cpr, and bankAccountId are required.\"}")
                    .build();
        }
        return handleRegister(request, () -> {
            try (MerchantProducer producer = new MerchantProducer()) {
                String response = producer.publishMerchantRegistered(request);
                return Response.accepted()
                        .entity(response)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GET
    @Path("/{merchantId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMerchant(@jakarta.ws.rs.PathParam("merchantId") String merchantId) {
        System.out.println("Received request to get merchant with ID: " + merchantId);
        if (merchantId == null || merchantId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"merchantId is required.\"}")
                    .build();
        }
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"error\": \"Merchant lookup is not supported via the api-gateway.\"}")
                .build();

    }

    @DELETE
    @Path("/{merchantId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMerchant(@jakarta.ws.rs.PathParam("merchantId") String merchantId) {
        System.out.println("Received request to delete merchant with ID: " + merchantId);
        if (merchantId == null || merchantId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"merchantId is required.\"}")
                    .build();
        }
        return handleDeregister(merchantId, () -> {
            try (MerchantProducer producer = new MerchantProducer()) {
                String response = producer.publishMerchantDeleted(merchantId);
                return Response.accepted()
                        .entity(response)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
