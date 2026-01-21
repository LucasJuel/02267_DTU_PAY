package org.g10.DTO;

public class ReportDTO {
    private String customerId;

    public ReportDTO(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}