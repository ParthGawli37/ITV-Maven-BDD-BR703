package pages;

import org.openqa.selenium.WebDriver;

import base.BaseClass;
import utils.ConfigReader;
import utils.LoggerUtils;

public class HomePage extends BaseClass {

    private final WebDriver driver;

    public HomePage() {
        this.driver = BaseClass.getDriver();
    }

    public void openHomePage() {
        String url = System.getProperty("url", ConfigReader.get("url"));
        if (url == null || url.isBlank()) {
            url = "https://www.district.in/";
        }
        driver.get(url);
        waitForPageLoad();
        LoggerUtils.info("Opened District.in: " + url);
    }

    public void openMoviesSection() {
        String moviesUrl = System.getProperty("moviesUrl", ConfigReader.get("moviesUrl"));
        if (moviesUrl == null || moviesUrl.isBlank()) {
            moviesUrl = "https://www.district.in/movies/mumbai-movie-tickets";
        }
        driver.get(moviesUrl);
        waitForPageLoad();
        LoggerUtils.info("Opened current movies listing: " + moviesUrl);
    }
}