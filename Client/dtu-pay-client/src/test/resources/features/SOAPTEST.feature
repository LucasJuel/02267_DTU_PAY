Feature: Payment_SOAP
  Scenario: Successful SOAP Payment
    Given a customer with name "HAHAHAHA", last name "Jespersen", and CPR "010101-0101"
    And the customer is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a merchant with name "Oliver", last name "Hansen", and CPR "010203-0405"
    And the merchant is registered with the bank with an initial balance of 1000 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the SOAP merchant initiates a payment for 10 kr by the customer
    Then the SOAP payment is successful
    And the balance of the customer at the bank is 990 kr
    And the balance of the merchant at the bank is 1010 kr

  Scenario: List of payments
    Given a customer with name "Jesper", last name "Jespersen", and CPR "010101-0101", who is registered with Simple DTU Pay
    And a merchant with name "Oliver", last name "Hansen", and CPR "010203-0405", who is registered with Simple DTU Pay
    Given a successful payment of "10" kr from the customer to the merchant
    When the manager asks for a list of payments
    Then the list contains a payments where customer "HAHAHAHA" paid "10" kr to merchant "Oliver"

  Scenario: Merchant is not known
    Given a merchant with name "Oliver", last name "Hansen", and CPR "010203-0405", who is registered with Simple DTU Pay
    When the merchant initiates a payment for "10" kr using merchant id "non-existent-id"
    Then the payment is not successful
    And an error message is returned saying "Merchant with id \"non-existent-id\" is unknown"

  Scenario: Customer is not known
    Given a customer with name "Jesper", last name "Jespersen", and CPR "010101-0101", who is registered with Simple DTU Pay
    When the customer initiates a payment for "10" kr using customer id "non-existent-id"
    Then the payment is not successful
    And an error message is returned saying "Customer with id \"non-existent-id\" is unknown"
