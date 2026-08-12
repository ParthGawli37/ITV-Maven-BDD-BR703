package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.HomePage;
import pages.MovieBookingPage;

public class DistrictSteps {

    private final HomePage homePage = new HomePage();
    private final MovieBookingPage bookingPage = new MovieBookingPage();

    @Given("I am on the District.in home page")
    public void open_home_page() {
        homePage.openHomePage();
    }

    @When("I open the Movies section")
    public void open_movies_section() {
        homePage.openMoviesSection();
    }

    @Then("at least 3 current movies should be listed")
    public void verify_current_movies() {
        bookingPage.assertCurrentMoviesAvailable();
    }

    @When("I select a current top movie")
    public void select_current_top_movie() {
        bookingPage.selectCurrentTopMovie();
    }

    @When("I select the first theatre with an available showtime")
    public void select_first_theatre() {
        bookingPage.selectFirstTheatreWithAvailableShowtime();
    }

    @When("I select the first available date")
    public void select_first_available_date() {
        bookingPage.selectFirstAvailableDate();
    }

    @When("I select the earliest available showtime")
    public void select_earliest_showtime() {
        bookingPage.selectFirstAvailableShowtime();
    }

    @Then("at least 2 seats should be available")
    public void verify_two_seats_available() {
        bookingPage.assertAtLeastTwoSeatsAvailable();
    }

    @When("I select the best adjacent seats for 2 people")
    public void select_best_seats() {
        bookingPage.selectBestAdjacentSeatsForTwo();
    }

    @When("I click the Proceed or Check In button")
    public void click_proceed_or_check_in() {
        bookingPage.clickProceedOrCheckIn();
    }

    @Then("the booking flow should continue to the next step")
    public void verify_booking_flow_continued() {
        bookingPage.assertBookingFlowContinued();
    }
}