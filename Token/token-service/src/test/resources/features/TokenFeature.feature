Feature: Token Management

  Scenario: Successful token request and use
    Given a registered customer "c123" without tokens
    When the customer requests 5 tokens
    Then the request is accepted
    And 5 tokens are added
    When the customer pays a merchant using one token
    Then the payment is successful
    And the customer now has 4 unused tokens

  Scenario: Customer attempts to reuse a consumed token
    Given a customer has used a token for a successful payment
    When the customer attempts to pay again with the same token
    Then the request is denied

  Scenario: Customer attempts to add more tokens with too many unused tokens
    Given a registered customer "c123" without tokens
    When the customer requests 2 tokens
    Then the request is accepted
    And 2 tokens are added
    When the customer requests 2 tokens
    Then the request is denied

  Scenario: Customer requests more tokens while having one left
    Given a registered customer "c123" without tokens
    When the customer requests 2 tokens
    And the customer pays a merchant using one token
    And the customer requests 5 tokens
    Then the request is accepted
    And the customer now has 6 unused tokens

  Scenario: Merchant attempts to process a payment with a fake token
    Given a registered merchant
    When the merchant attempts to process a payment with token "fakeToken123"
    Then the request is denied

  Scenario: Customer requests tokens that would exceed the maximum limit
    Given a registered customer "c123" without tokens
    When the customer requests 2 tokens
    And the customer pays a merchant using one token
    Then the customer now has 1 unused tokens
    When the customer requests 6 tokens
    Then the request is denied
