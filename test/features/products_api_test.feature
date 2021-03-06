Feature: Fruit list
  In order to make a great smoothie
  I need some fruit

  Scenario: List fruit (original)
    Given the system knows about the following fruit:
      | name       | color  |
      | banana     | yellow |
      | strawberry | red    |
    When the client requests GET /fruits
    Then the response should be JSON:
      """
      [
        {"name": "banana", "color": "yellow"},
        {"name": "strawberry", "color": "red"}
      ]
      """
  Scenario: Create product
    When the client create product "aaaa" and price 2.8
    Then the product's response should be JSON:
      """
        {"name": "strawberry", "color": "red1"}
      """
  Scenario: Create product
    When the client delete product "aaaa" and price 2.8
    Then the product's response should be JSON:
      """
        {"name": "strawberry", "color": "red1"}
      """