package org.g10.DTO;

import java.math.BigDecimal;

public class PaymentDTO {
    private String customerAccountId;
    private String merchantAccountId;
    private BigDecimal amount;
    private String message;


    public PaymentDTO() {
    }

    public PaymentDTO(String customerAccountId, String merchantAccountId, BigDecimal amount, String message) {
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
