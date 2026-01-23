package org.g10.DTO;
/**
 @author TheZoap
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

}
