@service
Feature: RabbitMQ basic publish

  Scenario: Publish and consume a message
    Given a RabbitMQ connection
    When I publish "hello" to the rabbit test queue
    Then I can consume "hello" from the rabbit test queue
