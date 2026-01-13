package org.thebois.endpoints.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.thebois.DTO.MerchantDTO;
import org.thebois.endpoints.services.MerchantService;


@Path("/merchant")
public class MerchantResource extends AbstractResource {
    private final MerchantService merchantService = new MerchantService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(MerchantDTO request) {
        // Adjust required fields to match your MerchantDTO
        if (request == null || request.getFirstName() == null
                || request.getCpr() == null || request.getBankAccountId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"companyName, cvr, and bankAccountId are required.\"}")
                    .build();
        }
        return handleRegister(request, () -> merchantService.register(request));
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
        try {
            return merchantService.getMerchant(merchantId);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"An unexpected error occurred: " + e.getMessage() + "\"}")
                    .build();
        }

    }

}
