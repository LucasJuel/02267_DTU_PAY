package com.client;

public class Customer {
    private String firstName;
    private String lastName;
    private String cpr;
    private String BankAccountNumber;
    private String id;
    
    public Customer(String firstName, String lastName, String cpr) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cpr = cpr;
    }
    
    public String getFirstName() {
        return firstName;
    }

     public void setFirstName(String firstName) {
        this.firstName = firstName;
     }

     public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;

    }

    public String getCpr() {
        return cpr;
    }

    public void setCpr(String cpr) {
        this.cpr = cpr;
    }
}