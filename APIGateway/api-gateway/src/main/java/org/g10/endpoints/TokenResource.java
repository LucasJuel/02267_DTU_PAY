package org.g10.endpoints;

import io.vertx.codegen.doc.Token;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.g10.DTO.TokenDTO;
import org.g10.services.TokenProducer;

import java.io.IOException;

/**
 @author TheZoap
 **/
@Path("/token")
public class TokenResource extends AbstractResource {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTokens(TokenDTO request) {
        if (request.getCustomerID() == null || request.getAmount() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Please enter a valid Customer ID and valid amount").build();
        }

        return handleRegister(request, () -> {
            try (TokenProducer producer = new TokenProducer()) {
                request.setType("ADD_TOKENS");
                TokenDTO response = producer.sendTokenRequest(request);
                return Response.ok().entity(response).build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GET
    @Path("/{customerID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToken(@PathParam("customerID") String customerID) {
        TokenDTO request = new TokenDTO();
        request.setCustomerID(customerID);
        request.setType("GET_TOKEN");

    try (TokenProducer producer = new TokenProducer()) {
        TokenDTO response = producer.sendTokenRequest(request);
        
        // Now you can extract fields from the TokenDTO
        String token = response.getToken();        
          
        
        if ("ERROR".equals(response.getType())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(response)
                    .build();
        }
        
        return Response.ok().entity(token).build();
    }
    catch (Exception e)  {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    }
}
