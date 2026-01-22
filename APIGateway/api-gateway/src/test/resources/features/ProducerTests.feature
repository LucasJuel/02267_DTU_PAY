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

  Scenario: Customer is deregistered from DTU Pay
    Given a RabbitMQ connection
    And a customer with first name "Charlie", last name "Brown" and cpr "112233-4455"
    And the customer have a bank account with the bank account id "cust-account-789"
    When I make a request to deregister the customer from DTU Pay
    Then the customer is deregistered successfully

  Scenario: Merchant is deregistered from DTU Pay
    Given a RabbitMQ connection
    And a merchant with first name "Diana" and last name "Prince" and cpr "556677-8899"
    And the merchant have a bank account with the bank account id "merch-account-012"
    When I make a request to deregister the merchant from DTU Pay
    Then the merchant is deregistered successfully
  