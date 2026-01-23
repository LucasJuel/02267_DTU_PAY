Feature: Account service unit behavior

  Scenario: Register customer directly via service
    Given a clean account storage
    When I register a customer with first name "Anna", last name "Andersen", CPR "123456-7890", and bank account "account-123"
    Then the stored customer matches the registration
  
  Scenario: Register customer fails
    Given a mocked account storage that fails on register
    When I attempt to register a customer with first name "Anna", last name "Andersen", CPR "123456-7890", and bank account "account-123"
    Then the registration fails with an error
  
  Scenario: Register merchant directly via service
    Given a clean account storage
    When I register a merchant with first name "Benny", last name "Bentsen", CPR "098765-4321", and bank account "account-456"
    Then the stored merchant matches the registration

  Scenario: Register merchant fails
    Given a mocked account storage that fails on register
    When I attempt to register a merchant with first name "Benny", last name "Bentsen", CPR "098765-4321", and bank account "account-456"
    Then the registration fails with an error

  Scenario: Look up unknown customer
    Given a clean account storage
    When I fetch a customer with id "missing-id"
    Then no customer is returned

  Scenario: Look up unknown merchant
    Given a clean account storage
    When I fetch a merchant with id "missing-id"
    Then no merchant is returned

  Scenario: Register same customer twice yields different ids
    Given a clean account storage
    When I register a customer with first name "Anna", last name "Andersen", CPR "123456-7890", and bank account "account-123"
    And I register the same customer again
    Then both customer registrations are stored separately

  Scenario: Register same merchant twice yields different ids
    Given a clean account storage
    When I register a merchant with first name "Benny", last name "Bentsen", CPR "098765-4321", and bank account "account-456"
    And I register the same merchant again
    Then both merchant registrations are stored separately

  Scenario: Customer and merchant storage are isolated
    Given a clean account storage
    When I register a customer with first name "Clara", last name "Clausen", CPR "567890-1234", and bank account "account-789"
    And I register a merchant with first name "Dan", last name "Dahl", CPR "222333-4444", and bank account "account-999"
    Then customer and merchant ids do not resolve across stores

  Scenario: Deregister customer
    Given a clean account storage
    When I register a customer with first name "Eva", last name "Eriksen", CPR "111222-3333", and bank account "account-111"
    Then the stored customer matches the registration
    When I deregister the customer with the customer id
    Then the customer is no longer in storage
  
  Scenario: Deregister customer fails
    Given a mocked account storage that fails on deregister
    When I register a customer with first name "Eva", last name "Eriksen", CPR "111222-3333", and bank account "account-111"
    Then the stored customer matches the registration
    When I attempt to deregister the customer with the customer id
    Then the deregistration fails with an error

  Scenario: Deregister merchant
    Given a clean account storage
    When I register a merchant with first name "Frank", last name "Frederiksen", CPR "444555-6666", and bank account "account-222"
    Then the stored merchant matches the registration
    When I deregister the merchant with the merchant id
    Then the merchant is no longer in storage
  
  Scenario: Deregister merchant fails
    Given a mocked account storage that fails on deregister
    When I register a merchant with first name "Frank", last name "Frederiksen", CPR "444555-6666", and bank account "account-222"
    Then the stored merchant matches the registration
    When I attempt to deregister the merchant with the merchant id
    Then the deregistration fails with an error

  Scenario: Deregister non-existent customer
    Given a clean account storage
    When I attempt to deregister a customer with id "non-existent-id"
    Then the deregistration completes without error

  Scenario: Deregister non-existent merchant
    Given a clean account storage
    When I attempt to deregister a merchant with id "non-existent-id"
    Then the deregistration completes without error
