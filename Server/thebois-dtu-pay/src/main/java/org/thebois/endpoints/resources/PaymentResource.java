package org.thebois.endpoints.resources;

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


import org.thebois.DTO.PaymentDTO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; 
import org.thebois.endpoints.services.PaymentService;

@Path("/payment")
public class PaymentResource {
    private final FileHandler fileHandler = new FileHandler("payments.json");


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
            // List payments implementation
            List<Map<String, Object>> payments = fileHandler.read();
            List<Map<String,Object>> merchantPayments = payments.stream()
                .filter(s -> merchantId.equals(s.get("merchantId")))
                .collect(Collectors.toList());

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


    //DTO for list for payments
    class PaymentListRequest {
        private String merchantId;
        public PaymentListRequest() {
        }
        public String getMerchantId() {
            return merchantId;
        }
        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }
    }
}   




