package org.g10.Services;

import org.g10.Utils.TokenStorage;

public class TokenService {

    private static final TokenStorage storage = new TokenStorage();

    public boolean requestAddTokens(String customerId, int amount) {
        try {
            int currentAmount = storage.getNumberOfTokens(customerId);

            if (currentAmount > 1) {
                throw new Exception("User has more than one token");
            }
            if (amount < 1 || amount > 5) {
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

    public void removeAllCustomerTokens(String customerId) throws Exception {
        storage.removeAllCustomerTokens(customerId);
    }

    public int getNumTokens(String customerId) {
        return storage.getNumberOfTokens(customerId);
    }
}
