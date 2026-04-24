package stepdefinitions;

import io.cucumber.java.en.*;
import pages.HomePage;

public class DistrictSteps {

    HomePage homePage = new HomePage();

    // Background
    @Given("I am on the District.in home page")
    public void open_home_page() {
        homePage.openHomePage();
    }

    @When("I click on the Search icon")
    public void click_search_icon() {
        homePage.clickSearchIcon();
    }

    @When("I click on the Movies link")
    public void click_movies_link() {
        homePage.clickMoviesLink();
    }

    // Scenario steps
    @When("I enter {string} in the search box")
    public void enter_movie_name(String movieName) {
        homePage.enterSearchText(movieName);
    }

    @Then("I should see the search results for {string}")
    public void verify_search_results(String expectedResult) {
        homePage.verifySearchResult(expectedResult);
    }
}