package org.g10.Utils;

import java.util.*;

public class TokenStorage {
    private static final Map<String, List<String>> customerTokens = new HashMap<>();
    private static final Map<String,String> tokenCustomers = new HashMap<>();

    public int getNumberOfTokens(String customerId) {
        List<String> tokens = customerTokens.get(customerId);
        return (tokens == null) ? 0 : tokens.size();
    }

    public String getToken(String customerId) {
        List<String> tokens = customerTokens.get(customerId);
        return tokens.getFirst();
    }

    public boolean consumeToken(String token) {
        String customerId = tokenCustomers.remove(token);
        if (customerId != null) {
            customerTokens.get(customerId).remove(token);
            return true;
        }
        return false;
    }

    public List<String> getCustomerTokens(String customerId) {
        return customerTokens.get(customerId);
    }

    public void addTokens(String customerId, int numberOfTokens) {
        List<String> currentTokens = customerTokens.computeIfAbsent(customerId, k -> new ArrayList<>());

        for (int i = 0; i < numberOfTokens; i++) {
            String token = UUID.randomUUID().toString();
            currentTokens.add(token);
            tokenCustomers.put(token, customerId);
        }
    }

    public String getCustomerFromToken(String token) {
        return tokenCustomers.get(token);
    }

    public void clear() {
        customerTokens.clear();
        tokenCustomers.clear();
    }
}


