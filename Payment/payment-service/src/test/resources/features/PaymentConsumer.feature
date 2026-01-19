Feature: Payment consumer persists payment events
  In order to keep track of payments sent through the queue
  As the payment service
  I want to store each consumed payment request in the storage handler

  Scenario: Store a payment event from JSON payload
    Given the payment service is running
    And a transaction request comes through rabbitMQ
    Given a transaction exists with the following details:
      | amount            | 100.50            |
      | customerAccountId | cust-123          |
      | merchantAccountId | merch-456         |
      | message           | Payment for order |
    When I register the payment with the payment service
    Then there is a message in the message queue containing the payment details
