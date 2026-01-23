package org.g10.DTO;
/**
 @author BertramKjær
 **/

public class MerchantDTO {
    private String firstName;
    private String lastName;
    private String cpr;
    private String bankAccountId;

    public MerchantDTO() {
    }

    public MerchantDTO(String firstName, String lastName, String cpr, String bankAccountId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cpr = cpr;
        this.bankAccountId = bankAccountId;
    }

    public String getFirstName() {
        return firstName;
    }


    public String getLastName() {
        return lastName;
    }

    public String getCpr() {
        return cpr;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setCpr(String cpr) {
    this.cpr = cpr;
    }
    public void setBankAccountId(String bankAccountId) {
        this.bankAccountId = bankAccountId;
    }
}
