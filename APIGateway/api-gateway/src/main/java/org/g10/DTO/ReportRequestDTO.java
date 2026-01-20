package org.g10.DTO;

public class ReportRequestDTO {
    private String customerId;

    public ReportRequestDTO(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}