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
