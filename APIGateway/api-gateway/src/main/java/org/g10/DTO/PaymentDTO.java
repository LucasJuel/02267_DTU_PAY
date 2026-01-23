package org.g10.DTO;

public class PaymentDTO {
    private String customerAccountId;
    private String merchantAccountId;
    private float amount;
    private String message;
    private String customerToken;


    public PaymentDTO() {
    }

    public PaymentDTO(String customerAccountId, String merchantAccountId, float amount, String message) {
        this.customerAccountId = customerAccountId;
        this.merchantAccountId = merchantAccountId;
        this.amount = amount;
        this.message = message;
    }

    public String getCustomerAccountId() {
        return customerAccountId;
    }

    public void setCustomerAccountId(String customerAccountId) {
        this.customerAccountId = customerAccountId;
    }

    public String getMerchantAccountId() {
        return merchantAccountId;
    }

    public void setMerchantAccountId(String merchantAccountId) {
        this.merchantAccountId = merchantAccountId;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getCustomerToken() {
        return customerToken;
    }
    public void setCustomerToken(String customerToken) {
        this.customerToken = customerToken;
    }

}
