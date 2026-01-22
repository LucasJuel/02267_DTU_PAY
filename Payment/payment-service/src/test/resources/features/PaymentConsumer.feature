Feature: Payment consumer persists payment events
  In order to keep track of payments sent through the queue
  As the payment service
  I want to store each consumed payment request in the storage handler

  Scenario: Store a payment event from JSON payload
    Given the payment service is running
    And the customer firstname "vivian" lastname "larsen" with cpr "12345678" is registered with the bank with an initial balance of 500 kr
    And the merchant firstname "torsten" lastname "torstenTO" with cpr "87654321" is registered with the bank with an initial balance of 500 kr
    Given a transaction between the customer and the merchant is initiated with amount 20.5 kr and message "Payment for 20.5 kr initiated"
    When I register the payment with the payment service
    Then the payment service should respond with a success message
    And  The customer has balance 479.5 on their bank account and merchant has balance 520.5
  
  Scenario: Verify payment event is stored in storage handler
    Given the payment service is running
    And the customer firstname "vivian" lastname "larsen" with cpr "12345678" is registered with the bank with an initial balance of 500 kr
    And the merchant firstname "torsten" lastname "torstenTO" with cpr "87654321" is registered with the bank with an initial balance of 500 kr
    Given a transaction between the customer and the merchant is initiated with amount 20.5 kr and message "Payment for 20.5 kr initiated"
    When I register the payment with the payment service
    Then the payment service should respond with a success message
    And the payment event should be stored in the storage handler with correct details:
      | merchantId | customerId | amount | message                      |
      | torstenTO  | vivian     | 20.5   | Payment for 20.5 kr initiated |
