Feature: Payment consumer persists payment events
  In order to keep track of payments sent through the queue
  As the payment service
  I want to store each consumed payment request in the storage handler


  Scenario: Store a payment event from JSON payload
    Given the customer firstname "vivian" lastname "larsen" with cpr "12345678" is registered with the bank with an initial balance of 500 kr
    And the merchant firstname "torsten" lastname "torstenTO" with cpr "87654321" is registered with the bank with an initial balance of 500 kr
    Given a transaction between the customer and the merchant is initiated with amount 20.5 kr and message "Payment for 20.5 kr initiated"
    When I register the payment with the payment service
    Then the payment service should respond with a success message
    And  The customer has balance 479.5 on their bank account and merchant has balance 520.5
  
  Scenario: Verify payment event is stored in storage handler
    Given the customer firstname "vivian" lastname "larsen" with cpr "12345678" is registered with the bank with an initial balance of 500 kr
    And the merchant firstname "torsten" lastname "torstenTO" with cpr "87654321" is registered with the bank with an initial balance of 500 kr
    Given a transaction between the customer and the merchant is initiated with amount 20.5 kr and message "Payment for 20.5 kr initiated"
    When I register the payment with the payment service
    Then the payment service should respond with a success message
    And the payment event should be stored in the storage handler with correct details

  Scenario: Payment initiated with amount less than zero
    Given the customer firstname "vivian" lastname "larsen" with cpr "12345678" is registered with the bank with an initial balance of 500 kr
    And the merchant firstname "torsten" lastname "torstenTO" with cpr "87654321" is registered with the bank with an initial balance of 500 kr
    Given a transaction between the customer and the merchant is initiated with amount -10 kr and message "Invalid payment"
    When I register the payment with the payment service
    Then the payment service should respond with a failure message indicating invalid amount

  Scenario: Payment initiated with zero amount
    Given the customer firstname "vivian" lastname "larsen" with cpr "12345678" is registered with the bank with an initial balance of 500 kr
    And the merchant firstname "torsten" lastname "torstenTO" with cpr "87654321" is registered with the bank with an initial balance of 500 kr
    Given a transaction between the customer and the merchant is initiated with amount 0 kr and message "Invalid payment"
    When I register the payment with the payment service
    Then the payment service should respond with a failure message indicating invalid amount
  
  Scenario: Payment initiated with no message
    Given the customer firstname "vivian" lastname "larsen" with cpr "12345678" is registered with the bank with an initial balance of 500 kr
    And the merchant firstname "torsten" lastname "torstenTO" with cpr "87654321" is registered with the bank with an initial balance of 500 kr
    Given a transaction between the customer and the merchant is initiated with amount 15 kr and no message
    When I register the payment with the payment service
    Then the payment message should default to standard message
    And  The customer has balance 485 on their bank account and merchant has balance 515

  Scenario: Payment fails with an unknown bank exception
    Given the customer firstname "vivian" lastname "larsen" with cpr "12345678" is registered with the bank with an initial balance of 500 kr
    And the merchant firstname "torsten" lastname "torstenTO" with cpr "87654321" is registered with the bank with an initial balance of 500 kr
    Given the bank service is mocked to throw a bank exception on transfer
    Given a transaction between the customer and the merchant is initiated with amount 30 kr and message "Payment for 30 kr initiated"
    When I register the payment with the payment service
    Then the payment service should respond with a failure message indicating error

  Scenario: Get all payments for a specific merchant
    Given the customer firstname "vivian" lastname "larsen" with cpr "12345678" is registered with the bank with an initial balance of 500 kr
    And the merchant firstname "torsten" lastname "torstenTO" with cpr "87654321" is registered with the bank with an initial balance of 500 kr
    Given a transaction between the customer and the merchant is initiated with amount 25 kr and message "Payment for 25 kr initiated"
    And another transaction between the customer and the merchant is initiated with amount 40 kr and message "Payment for 40 kr initiated"
    When I register both payments with the payment service
    Then I request all payments for merchant "torstenTO"
    And I should receive a list containing both payment events with correct details
