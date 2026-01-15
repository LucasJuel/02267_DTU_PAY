Feature: Account tests
  Scenario: Register a new account
    Given a customer with name "Anna", last name "Andersen", and CPR "123456-7890"
    And the account service is running
    When I register the customer with the account service
    Then there is a message in the account queue with the customer details