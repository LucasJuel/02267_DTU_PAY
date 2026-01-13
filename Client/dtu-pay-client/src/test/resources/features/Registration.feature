  Feature: Registration_SOAP
  Scenario: Successful customer registration
    Given a customer with name "Alice", last name "Jensen", and CPR "111111-1111"
    And the customer is registered with the bank with an initial balance of 1000 kr
    When the customer attempts to register with Simple DTU Pay using their bank account
    Then the customer registration is successful

  Scenario: Customer registration rejected due to mismatched details
    Given a customer with name "Bob", last name "Lee", and CPR "222222-2222"
    And the customer is registered with the bank with an initial balance of 1000 kr
    And the customer details are changed to name "Bobby", last name "Lee", and CPR "999999-9999"
    When the customer attempts to register with Simple DTU Pay using their bank account
    Then the customer registration fails with status 400

  Scenario: Customer registration rejected due to unknown bank account
    Given a customer with name "Cara", last name "Nash", and CPR "333333-3333"
    When the customer attempts to register with Simple DTU Pay using bank account "does-not-exist"
    Then the customer registration fails with status 404

  Scenario: Successful merchant registration
    Given a merchant with name "Dani", last name "Olsen", and CPR "444444-4444"
    And the merchant is registered with the bank with an initial balance of 1000 kr
    When the merchant attempts to register with Simple DTU Pay using their bank account
    Then the merchant registration is successful

  Scenario: Merchant registration rejected due to mismatched details
    Given a merchant with name "Eli", last name "Perez", and CPR "555555-5555"
    And the merchant is registered with the bank with an initial balance of 1000 kr
    And the merchant details are changed to name "Elijah", last name "Perez", and CPR "888888-8888"
    When the merchant attempts to register with Simple DTU Pay using their bank account
    Then the merchant registration fails with status 400

  Scenario: Merchant registration rejected due to unknown bank account
    Given a merchant with name "Fay", last name "Quinn", and CPR "666666-6666"
    When the merchant attempts to register with Simple DTU Pay using bank account "does-not-exist"
    Then the merchant registration fails with status 404
