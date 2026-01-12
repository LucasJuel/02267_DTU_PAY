package org.thebois.endpoints.resources;

import dtu.ws.fastmoney.Account;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.thebois.DTO.CustomerDTO;


@Path("/bankCustomer")
public class CustomerResource {
    private final BankService bank = new BankService_Service().getBankServicePort();

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerCustomer(CustomerDTO request) {
        if (request == null || request.getFirstName() == null || request.getLastName() == null
                || request.getCpr() == null || request.getBankAccountNumber() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"firstName, lastName, cpr, and bankAccountId are required.\"}")
                    .build();
        }

        try {
            Account account = bank.getAccount(request.getBankAccountNumber());
            if (account == null || account.getUser() == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Bank account not found.\"}")
                        .build();
            }

            String cpr = account.getUser().getCprNumber();
            String firstName = account.getUser().getFirstName();
            String lastName = account.getUser().getLastName();
            if (!request.getCpr().equals(cpr)
                    || !request.getFirstName().equals(firstName)
                    || !request.getLastName().equals(lastName)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Bank account details do not match provided customer data.\"}")
                        .build();
            }

            return Response.status(Response.Status.CREATED)
                    .entity(account.getId())
                    .build();
        } catch (BankServiceException_Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Bank account not found: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to register customer: " + e.getMessage() + "\"}")
                    .build();
        }
    }

}
