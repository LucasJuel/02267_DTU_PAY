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
    Given a registered customer "c321" without tokens
    And the customer has used a token for a successful payment
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
  
  Scenario: Customer requests less than the minimum allowed tokens
    Given a registered customer "c123" without tokens
    When the customer requests 0 tokens
    Then the request is denied

  Scenario: A request for adding tokens fails due to customerId being null
    Given a customer with null customerId
    When the customer attempts to request 5 tokens
    Then the request fails with an error
  
  Scenario: A request for adding tokens fails due to customerId being empty
    Given a customer with empty customerId
    When the customer attempts to request 3 tokens
    Then the request fails with an error

  Scenario: A request for consuming a token for customer with customerId null
    Given a customer with null customerId
    When the customer attempts to consume a token
    Then the request fails with an error

  Scenario: A non-existing customer tries to request tokens
    Given a non-existing customer "c999"
    When the customer attempts to get their token
    Then the request fails with an error
  
  Scenario: A customer with more than one token has all tokens removed
    Given a registered customer "c123" with 3 tokens
    When all tokens are removed for the customer
    Then the customer has zero tokens
  
  Scenario: A Token DTO is created and its fields are verified
    Given a token value "tokenXYZ" and customerId "custABC"
    And an amount of 4 tokens, type "VALIDATE_TOKEN", and error message "None"
    When a Token DTO is created with these values
    Then the Token DTO has
