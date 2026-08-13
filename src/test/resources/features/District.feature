Feature: District.in movie booking
  Validate District's real user journey from location selection and search to movie booking.

  @SmokeTest
  Scenario Outline: Search for a current movie in Mumbai
    Given I am on the District.in home page
    And Mumbai is the selected District location
    When I search for the movie "<movie>"
    And I select Movies in the search results
    And I open the movie "<movie>" from the search results
    Then the "<movie>" movie page should be open

    Examples:
      | movie                      |
      | Spider-Man: Brand New Day  |
      | The Odyssey                |

  @RegressionTest
  Scenario Outline: Book two seats for a selected current movie up to the proceed step
    Given I am on the District.in home page
    And Mumbai is the selected District location
    When I search for the movie "<movie>"
    And I select Movies in the search results
    And I open the movie "<movie>" from the search results
    And I select the first available date
    And I select the first theatre with an available showtime
    And I select the earliest available showtime
    Then at least 2 seats should be available
    When I select the best adjacent seats for 2 people
    And I click the Proceed or Check In button
    Then the booking flow should continue to the next step

    Examples:
      | movie                      |
      | Spider-Man: Brand New Day  |
      | The Odyssey                |
