package org.g10.endpoints;

import org.g10.DTO.ReportDTO;
import org.g10.services.ReportingProducer;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("customer/{id}/report")
public class CustomerReportResource extends AbstractResource {
    @POST
    public Response generateReport(@jakarta.ws.rs.PathParam("id") String id) {
        System.out.println("Received request to get report for customer with ID: " + id);
        if (id == null || id.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"customerId is required.\"}")
                    .build();
        }
        try {
            try (ReportingProducer producer = new ReportingProducer()) {
                ReportDTO report = new ReportDTO(id, "customer");
                String response = producer.publishReportRequest(report);
                return Response.ok()
                        .entity(response)
                        .build();
            }
        } catch (java.util.concurrent.TimeoutException e) {
            return Response.status(Response.Status.REQUEST_TIMEOUT)
                    .entity("{\"error\": \"Reporting service did not respond in time.\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to generate report: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}

@Path("merchant/{id}/report")
class MerchantReportResource extends AbstractResource {
    @POST
    public Response generateReport(@jakarta.ws.rs.PathParam("id") String id) {
        System.out.println("Received request to get report for merchant with ID: " + id);
        if (id == null || id.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"merchantId is required.\"}")
                    .build();
        }
        try {
            try (ReportingProducer producer = new ReportingProducer()) {
                ReportDTO report = new ReportDTO(id, "merchant");
                String response = producer.publishReportRequest(report);
                return Response.ok()
                        .entity(response)
                        .build();
            }
        } catch (java.util.concurrent.TimeoutException e) {
            return Response.status(Response.Status.REQUEST_TIMEOUT)
                    .entity("{\"error\": \"Reporting service did not respond in time.\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to generate report: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
