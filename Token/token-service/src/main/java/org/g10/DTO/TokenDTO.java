package org.g10.DTO;


public class TokenDTO {
    private String type;
    private String customerID;
    private int amount;
    private String token;
    private String errorMSG;

    public TokenDTO() {}

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getCustomerID() {
        return customerID;
    }
    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getErrorMSG() {
        return errorMSG;
    }
    public void setErrorMSG(String errorMSG) {
        this.errorMSG = errorMSG;
    }



}
