Feature: Account tests
  Scenario: Register a new customer account
    Given a customer with name "Anna", last name "Andersen", and CPR "123456-7890"
    And the account service is running
    When I register the customer with the account service
    Then there is a message in the account queue with the customer details

  Scenario: Register a new merchant account
    Given a merchant with name "Benny", last name "Bentsen", and CPR "098765-4321"
    And the account service is running
    When I register the merchant with the account service
    Then there is a message in the account queue with the merchant details