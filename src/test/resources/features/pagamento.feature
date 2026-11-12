Feature: Payment flow with Mercado Pago
  As the billing service
  I want to generate a budget and process payment
  So that the service order can be financially closed

  Scenario: Successful payment approval after checkout
    Given a service order ready for billing
    When a budget is generated with total 250.00
    And a checkout is created for the budget
    And Mercado Pago approves the payment
    Then the payment status should be APROVADO
    And a payment approved event should be published

  Scenario: Payment failure publishes failure event
    Given a service order ready for billing
    When a budget is generated with total 100.00
    And a checkout is created for the budget
    And Mercado Pago rejects the payment
    Then the payment status should be RECUSADO
    And a payment failed event should be published
