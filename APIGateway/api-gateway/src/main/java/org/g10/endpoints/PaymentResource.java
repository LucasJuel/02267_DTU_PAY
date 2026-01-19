package org.g10.endpoints;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.g10.services.PaymentProducer;
import org.g10.DTO.PaymentDTO;

@Path("/payment")
public class PaymentResource {


    // Implementation of PaymentResource class

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pay(PaymentDTO request) {
        System.out.println("Processing payment for merchant: " + request.getMerchantAccountId());

        try {
            try (PaymentProducer producer = new PaymentProducer()) {
                producer.publishPaymentRequested(request);
                return Response.accepted()
                        .entity("{\"status\": \"queued\"}")
                        .build();
            }
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
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"error\": \"Payment listing is not supported via the api-gateway.\"}")
                .build();
    }
}
