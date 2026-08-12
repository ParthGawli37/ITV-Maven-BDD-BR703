Feature: District.in movie booking
  End-to-end validation of the current movie listing and movie-ticket booking flow.

  Background:
    Given I am on the District.in home page
    When I open the Movies section

  @SmokeTest
  Scenario: Current movies are available for booking
    Then at least 3 current movies should be listed

  @RegressionTest
  Scenario: Book two seats for a current movie up to the proceed step
    And I select a current top movie
    And I select the first available date
    And I select the first theatre with an available showtime
    And I select the earliest available showtime
    Then at least 2 seats should be available
    When I select the best adjacent seats for 2 people
    And I click the Proceed or Check In button
    Then the booking flow should continue to the next step
