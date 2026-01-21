Feature: Token Management

  Scenario: Successful token request and use
    Given a registered customer "cid-123" without tokens
    When the customer requests 5 tokens
    Then the request is accepted and 5 tokens are added
    When the customer pays a merchant using one token
    Then the payment is successful
    And the customer now has 4 unused tokens

  Scenario: Customer attempts to reuse a consumed token
    Given a customer has used a token for a successful payment
    When the customer attempts to pay again with the same token
    Then the payment is denied

