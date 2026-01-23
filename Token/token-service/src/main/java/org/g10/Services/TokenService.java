package org.g10.Services;

import org.g10.Utils.TokenStorage;
/**
 @author Martin-Surlykke
 **/
public class TokenService {

    private static final TokenStorage storage = new TokenStorage();

    public boolean requestAddTokens(String customerId, int amount) {

        try {
            int currentAmount = storage.getNumberOfTokens(customerId);
            if (customerId == null || customerId.isEmpty()) {
                throw new Exception("Customer ID is empty");
            }
            else if (currentAmount > 1) {
                throw new Exception("User has more than one token");
            }
            else if (amount < 1 || amount > 5) {
                throw new Exception("Addition amount must be between 1 and 5");
            }
            else {
                storage.addTokens(customerId, amount);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to add tokens: " + e.getMessage());
            return false;
        }

    }

    public String consumeToken(String token) throws Exception {
        String customerId = storage.getCustomerFromToken(token);
        if (customerId == null) {
            System.err.println("Customer id not found");
            throw new Exception("Invalid token");
        }

        boolean consumed = storage.consumeToken(token);
        if (!consumed) {
            System.err.println("Consumed token does not match stored token");
            throw new Exception("Consumption of token failed");

        }
        return customerId;

    }

    public String requestGetToken(String customerId) throws Exception {
        String token = storage.getToken(customerId);
        if (token == null) {
            throw new Exception("Token not found in system");
        }
        return token;
    }

    public void removeAllCustomerTokens(String customerId) {
        storage.removeAllCustomerTokens(customerId);
    }

    public int getNumTokens(String customerId) {
        return storage.getNumberOfTokens(customerId);
    }

    public String getToken(String customerID) {
        return storage.getToken(customerID);
    }

    public void clearStorage () {
        storage.clear();
    }
}
