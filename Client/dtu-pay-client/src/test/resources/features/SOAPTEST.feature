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


