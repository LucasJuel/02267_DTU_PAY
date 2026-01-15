package org.thebois.endpoints.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.thebois.DTO.PaymentDTO;
import java.util.List;
import java.util.Map;
import org.thebois.endpoints.services.PaymentService;
import org.thebois.utils.StorageHandler;

@Path("/payment")
public class PaymentResource {


    // Implementation of PaymentResource class

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pay(PaymentDTO request) {
        System.out.println("Processing payment for merchant: " + request.getMerchantAccountId());

        PaymentService paymentService = new PaymentService();
        try {
            return paymentService.register(request);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"An unexpected error occurred: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/list/{merchantId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPayments(@jakarta.ws.rs.PathParam("merchantId") String merchantId){
        try{
            // Use StorageHandler to retrieve payments for the merchant
            List<Map<String, Object>> merchantPayments = StorageHandler.getInstance()
                    .getPaymentsByMerchant(merchantId);

            return Response.ok()
                    .entity(merchantPayments)
                    .build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to process list of payments: " + e.getMessage() + "\"}")
                    .build();
        }

    }
}   
