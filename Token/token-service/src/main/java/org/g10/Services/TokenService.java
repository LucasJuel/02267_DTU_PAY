package org.g10.Services;

import org.g10.Utils.TokenStorage;

import java.util.ArrayList;
import java.util.List;

public class TokenService {

    private static final TokenStorage storage = new TokenStorage();

    public void requestAddTokens(String customerId, int amount) {

        try {


            int currentAmount = storage.getNumberOfTokens(customerId);
            if (currentAmount > 1) {
                return;
            }
            if (currentAmount + amount > 6) {
                return;
            }

            if (amount < 1 || amount > 5)
                return;


            storage.addTokens(customerId, amount);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String consumeToken(String token) throws Exception {
        String customerId = storage.getCustomerFromToken(token);
        if (customerId == null) {
            throw new Exception("Invalid token");
        }

        boolean consumed = storage.consumeToken(token);
        if (!consumed) {
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

    public int getNumTokens(String customerId) {
        return storage.getNumberOfTokens(customerId);
    }

    public void clearStorage() {
        storage.clear();
    }
}
