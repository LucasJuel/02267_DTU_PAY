@RabbitMQ
Feature: RabbitMQ producers
  Scenario: Customer is registered with DTU Pay
    Given a RabbitMQ connection
    And a customer with first name "Alice", last name "Smith" and cpr "123456-7890"
    And the customer have a bank account with the bank account id "cust-account-123"
    When I make a request to register the customer in DTU Pay
    Then the customer is registered successfully

  Scenario: Merchant is registered with DTU Pay
    Given a RabbitMQ connection
    And a merchant with first name "Bob" and last name "Johnson" and cpr "098765-4321"
    And the merchant have a bank account with the bank account id "merch-account-456"
    When I make a request to register the merchant in DTU Pay
    Then the merchant is registered successfully


  Scenario: Customer called for report of payments
    Given a RabbitMQ connection
    And a customer with id "cust-001"
    When I make a request for a report of payments for the customer
    Then the report of payments is returned successfully
  
  Scenario: Merchant called for report of payments
    Given a RabbitMQ connection
    And a merchant with id "merch-001"
    When I make a request for a report of payments for the merchant
    Then the report of payments is returned successfully

  Scenario: Manager called for report of all payments
    Given a RabbitMQ connection
    And the manager requests a report
    When I make a request for a report of all payments for the manager
    Then the manager report is returned successfully
