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

@Path("/payment")
public class PaymentResource {

    FileHandler fileHandler = new FileHandler("payments.json");
    // Implementation of PaymentResource class

    @POST
    @Path("/pay")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pay(PaymentRequest paymentDetails) {
        // Payment processing implementation
        fileHandler.write(paymentDetails);
        return "Payment processed";
    }

    @GET
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPayments(PaymentListRequest paymentListRequest) {
        // List payments implementation
        String payments = fileHandler.read();
        //Json serialize payments
        List<String> paymentArray = Arrays.asList(payments.split("\n"))
            .stream()
            .filter(s -> s.contains(paymentListRequest.getMerchantId()))
            .collect(Collectors.toList());
        return paymentArray.toString();
    }
}


//DTO for pay
class PaymentRequest {
    private int amount;
    private String customerId;
    private String merchantId;

    public PaymentRequest() {
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
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
