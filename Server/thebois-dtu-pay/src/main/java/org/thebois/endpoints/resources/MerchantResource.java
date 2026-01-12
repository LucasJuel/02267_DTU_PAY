package org.thebois.endpoints.resources;

import dtu.ws.fastmoney.Account;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.thebois.DTO.MerchantDTO;
import org.thebois.endpoints.services.MerchantService;


@Path("/merchant")
public class MerchantResource {
    private final MerchantService merchantService = new MerchantService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    public Response register(MerchantDTO request) {
        System.out.println("Received registration request for Merchant: " + request.getFirstName() + " " + request.getLastName() + ", CPR: " + request.getCpr() + ", Bank Account: " + request.getBankAccountId());
        if (request == null || request.getFirstName() == null || request.getLastName() == null
                || request.getCpr() == null || request.getBankAccountId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"firstName, lastName, cpr, and bankAccountId are required.\"}")
                    .build();
        }
        try {
            return merchantService.register(request);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"An unexpected error occurred: " + e.getMessage() + "\"}")
                    .build();
        }
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
