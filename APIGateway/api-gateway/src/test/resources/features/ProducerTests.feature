Feature: RabbitMQ producers

  Scenario: Customer producer publishes a customer event
    Given a RabbitMQ connection
    When I publish a customer registration event
    Then the customer event is available on the customer queue

  Scenario: Merchant producer publishes a merchant event
    Given a RabbitMQ connection
    When I publish a merchant registration event
    Then the merchant event is available on the merchant queue

  Scenario: Payment producer publishes a payment event
    Given a RabbitMQ connection
    When I publish a payment request event
    Then the payment event is available on the payment queue
