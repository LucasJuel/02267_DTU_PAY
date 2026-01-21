package org.g10.DTO;

public class ReportDTO {
    private String accountId;
    private String accountType;

    public ReportDTO() {
    }

    public ReportDTO(String id, String accountType) {
        this.accountId = id;
        this.accountType = accountType;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}