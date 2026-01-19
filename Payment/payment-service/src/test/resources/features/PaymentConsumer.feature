Feature: Payment consumer persists payment events
  In order to keep track of payments sent through the queue
  As the payment service
  I want to store each consumed payment request in the storage handler

  Scenario: Store a payment event from JSON payload
    Given a fresh payment store
    When the payment consumer handles payload:
      """
      {
        "customerAccountId": "cust-42",
        "merchantAccountId": "merch-99",
        "amount": 125.75,
        "message": "Paying for coffee"
      }
      """
    Then the payment storage contains the following entry:
      | customerAccountId | cust-42         |
      | merchantAccountId | merch-99        |
      | amount            | 125.75          |
      | message           | Paying for coffee |
