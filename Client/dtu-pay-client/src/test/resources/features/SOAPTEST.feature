Feature: Payment_SOAP
  Scenario: Successful SOAP Payment
    Given a customer with name "Susanne", last name "Baldwin", and CPR "030264-4421"
    And the customer is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a merchant with name "Danielli", last name "Oliver", and CPR "131161-3045"
    And the merchant is registered with the bank with an initial balance of 1000 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the SOAP merchant initiates a payment for 10 kr by the customer
    Then the SOAP payment is successful
    And the balance of the customer at the bank is 990 kr
    And the balance of the merchant at the bank is 1010 kr