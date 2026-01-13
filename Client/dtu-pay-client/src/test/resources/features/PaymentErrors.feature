Feature: Payment_Errors_SOAP
  Scenario: Payment rejected when customer account is unknown
    Given a merchant with name "Hana", last name "Voss", and CPR "888888-8888"
    And the merchant is registered with the bank with an initial balance of 1000 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    And the customer uses bank account id "does-not-exist"
    When the SOAP merchant initiates a payment for 10 kr by the customer
    Then the SOAP payment fails with status 500
    And the balance of the merchant at the bank is 1000 kr

  Scenario: Payment rejected when merchant account is unknown
    Given a customer with name "Iris", last name "Young", and CPR "999999-9999"
    And the customer is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the merchant uses bank account id "does-not-exist"
    When the SOAP merchant initiates a payment for 10 kr by the customer
    Then the SOAP payment fails with status 404
    And the balance of the customer at the bank is 1000 kr

  Scenario: Payment rejected for zero amount
    Given a customer with name "Kara", last name "One", and CPR "111111-0000"
    And the customer is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a merchant with name "Lars", last name "Two", and CPR "121212-1212"
    And the merchant is registered with the bank with an initial balance of 1000 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the SOAP merchant initiates a payment for 0 kr by the customer
    Then the SOAP payment fails with status 400
    And the balance of the customer at the bank is 1000 kr
    And the balance of the merchant at the bank is 1000 kr

  Scenario: Payment rejected for negative amount
    Given a customer with name "Milo", last name "Three", and CPR "131313-1313"
    And the customer is registered with the bank with an initial balance of 1000 kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a merchant with name "Nia", last name "Four", and CPR "141414-1414"
    And the merchant is registered with the bank with an initial balance of 1000 kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the SOAP merchant initiates a payment for -10 kr by the customer
    Then the SOAP payment fails with status 400
    And the balance of the customer at the bank is 1000 kr
    And the balance of the merchant at the bank is 1000 kr
