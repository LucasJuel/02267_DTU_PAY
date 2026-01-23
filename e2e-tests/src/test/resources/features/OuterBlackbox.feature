@e2e
Feature: Outer Blackbox Payment
  Scenario: Customer pays merchant using public DTU Pay API
    Given the DTU Pay system is running
    And a customer is created in the bank with balance 1000 kr
    And a merchant is created in the bank with balance 500 kr
    And the customer is registered with DTU Pay via the public API
    And the merchant is registered with DTU Pay via the public API
    And the customer provides the merchant with a token for payment
    When the merchant initiates a payment of 75 kr via the public API
    Then the customer bank balance eventually becomes 925 kr
    And the merchant bank balance eventually becomes 575 kr

  Scenario: Merchant attempts payment with invalid token
    Given the DTU Pay system is running
    And a merchant is created in the bank with balance 500 kr
    And the merchant is registered with DTU Pay via the public API
    When the merchant initiates a payment of 75 kr via the public API with the invalid token "INVALID_TOKEN"
    Then the payment is rejected with an error message indicating invalid token

  Scenario: Customer attempts to request more tokens than allowed
    Given the DTU Pay system is running
    And a customer is created in the bank with balance 1000 kr
    And the customer is registered with DTU Pay via the public API
    When the customer requests a new set of 5 tokens via the public API
    Then the token request is rejected with an error message indicating token limit exceeded


