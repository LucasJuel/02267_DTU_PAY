Feature: Payment_SOAP
  Scenario: Successful SOAP Payment
    Given a customer with name "Jesper", last name "Jespersen", and CPR "010101-0101"
    And the customer is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a merchant with name "Oliver", last name "Hansen", and CPR "010203-0405"
    And the merchant is registered with the bank with an initial balance of 1000 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the SOAP merchant initiates a payment for 10 kr by the customer with the description "Test payment"
    Then there exists a SOAP payment with the description "Test payment"
    And the balance of the customer at the bank is 990 kr
    And the balance of the merchant at the bank is 1010 kr

  Scenario: SOAP Payment rejected for zero amount
    Given a customer with name "Zero", last name "Amount", and CPR "020202-0202"
    And the customer is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a merchant with name "Mika", last name "Wong", and CPR "030303-0303"
    And the merchant is registered with the bank with an initial balance of 1000 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the SOAP merchant initiates a payment for 0 kr by the customer with the description "Zero amount test"
    Then the SOAP payment fails with status 400
    And there does not exists a SOAP payment with the description "Zero amount test"
    And the balance of the customer at the bank is 1000 kr
    And the balance of the merchant at the bank is 1000 kr

  Scenario: SOAP Payment rejected for insufficient customer balance
    Given a customer with name "Ivy", last name "Low", and CPR "060606-0606"
    And the customer is registered with the bank with an initial balance of 5 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a merchant with name "Omar", last name "Bay", and CPR "070707-0707"
    And the merchant is registered with the bank with an initial balance of 1000 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the SOAP merchant initiates a payment for 10 kr by the customer with the description "Insufficient funds test"
    Then there does not exists a SOAP payment with the description "Insufficient funds test"
    And the balance of the customer at the bank is 5 kr
    And the balance of the merchant at the bank is 1000 kr
