Feature: Reporting Service
    Scenario: Generate report for customer
        Given a clean account storage
        And I register a customer with first name "David", last name "Dahl", CPR "112233-4455", and bank account "account-123"
        When I generate a report for the customer
        Then the report returns an empty list of payments