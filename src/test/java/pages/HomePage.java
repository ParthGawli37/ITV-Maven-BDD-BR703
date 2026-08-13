package pages;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import base.BaseClass;
import utils.ConfigReader;
import utils.LoggerUtils;

public class HomePage extends BaseClass {

    public HomePage() {
        super();
    }

    private WebDriver driver() {
        WebDriver driver = BaseClass.getDriver();
        if (driver == null) {
            throw new IllegalStateException("WebDriver has not been initialized. Check Cucumber hooks.");
        }
        return driver;
    }

    public void openHomePage() {
        String url = System.getProperty("url", ConfigReader.get("url"));
        if (url == null || url.isBlank()) {
            url = "https://www.district.in/";
        }
        driver().get(url);
        waitForPageLoad();
        LoggerUtils.info("Opened District.in: " + url);
    }

    /**
     * Uses District's visible location control rather than relying on a
     * location-specific URL. If the location is already Mumbai, the method
     * leaves it unchanged.
     */
    public void ensureMumbaiLocation() {
        WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(10));
        String body = driver().findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
        if (body.contains("mumbai") && !hasVisibleLocationPicker()) {
            LoggerUtils.info("Mumbai already appears to be the active District location");
            return;
        }

        List<WebElement> triggers = driver().findElements(By.xpath(
                "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'select location')"
                + " or @aria-label='Select Location']"));
        WebElement trigger = firstVisible(triggers);
        if (trigger != null) {
            click(trigger);
        }

        WebElement locationSearch = waitForSearchInput(wait, "location");
        if (locationSearch != null) {
            locationSearch.clear();
            locationSearch.sendKeys("Mumbai");
        }

        WebElement mumbai = wait.until(d -> {
            for (WebElement element : d.findElements(By.xpath(
                    "//*[self::button or self::a or self::li or self::div or self::span][normalize-space()='Mumbai']"))) {
                if (isVisible(element) && element.isEnabled()) return element;
            }
            return null;
        });

        click(mumbai);
        waitForPageLoad();
        LoggerUtils.info("District location set to Mumbai");
    }

    public void openMoviesSection() {
        String moviesUrl = System.getProperty("moviesUrl", ConfigReader.get("moviesUrl"));
        if (moviesUrl == null || moviesUrl.isBlank()) {
            moviesUrl = "https://www.district.in/movies/mumbai-movie-tickets";
        }
        driver().get(moviesUrl);
        waitForPageLoad();
        LoggerUtils.info("Opened current movies listing: " + moviesUrl);
    }

    public void searchMovie(String movieTitle) {
        WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(15));

        WebElement searchTrigger = firstVisible(driver().findElements(By.xpath(
                "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'search for events, movies and restaurants')"
                + " or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'search')]")));
        if (searchTrigger != null && !isInput(searchTrigger)) {
            click(searchTrigger);
        }

        WebElement input = waitForSearchInput(wait, "movie");
        Assert.assertNotNull(input, "District search input was not found");
        input.clear();
        input.sendKeys(movieTitle);

        wait.until(d -> !findMovieResults(movieTitle).isEmpty());
        LoggerUtils.info("Search results loaded for movie: " + movieTitle);
    }

    public void clickMoviesSearchFilter() {
        WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(8));
        List<WebElement> candidates = driver().findElements(By.xpath(
                "//*[@role='dialog']//*[self::button or @role='tab' or self::a or self::div][normalize-space()='Movies']"
                + " | //*[@role='tab'][normalize-space()='Movies']"
                + " | //button[normalize-space()='Movies']"));

        WebElement movies = firstVisible(candidates);
        if (movies == null) {
            LoggerUtils.info("Search did not expose a separate Movies filter; continuing with movie results");
            return;
        }

        click(movies);
        wait.until(d -> !findMovieResultsFromPage(driver()).isEmpty());
        LoggerUtils.info("Selected Movies in District search");
    }

    public void openMovieFromSearch(String movieTitle) {
        WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(15));
        WebElement result = wait.until(d -> {
            for (WebElement element : findMovieResults(movieTitle)) {
                if (isVisible(element) && element.isEnabled()) return element;
            }
            return null;
        });

        Assert.assertNotNull(result, "Movie search result not found: " + movieTitle);
        click(result);
        waitForPageLoad();

        String pageText = driver().findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
        Assert.assertTrue(pageText.contains(movieTitle.toLowerCase(Locale.ROOT))
                        || driver().getCurrentUrl().toLowerCase(Locale.ROOT).contains(slug(movieTitle)),
                "Opened page does not appear to be for movie: " + movieTitle + " | URL=" + driver().getCurrentUrl());
        LoggerUtils.info("Opened movie page: " + movieTitle + " | URL=" + driver().getCurrentUrl());
    }

    private List<WebElement> findMovieResults(String movieTitle) {
        String title = movieTitle.replace("'", "\\'");
        return driver().findElements(By.xpath(
                "//*[self::a or self::button or @role='button' or @role='link'][contains(translate(normalize-space(.),"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),translate('" + title + "',"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'))]"));
    }

    private List<WebElement> findMovieResultsFromPage(WebDriver driver) {
        return driver.findElements(By.xpath(
                "//*[self::a or self::button or @role='button' or @role='link'][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'spider-man')"
                + " or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'the odyssey')]"));
    }

    private WebElement waitForSearchInput(WebDriverWait wait, String kind) {
        return wait.until(d -> {
            for (WebElement input : d.findElements(By.xpath(
                    "//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'search')]"
                    + " | //textarea[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'search')]"))) {
                if (isVisible(input) && input.isEnabled()) return input;
            }
            return null;
        });
    }

    private boolean hasVisibleLocationPicker() {
        return firstVisible(driver().findElements(By.xpath(
                "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'select location')"
                + " or @aria-label='Select Location']"))) != null;
    }

    private WebElement firstVisible(List<WebElement> elements) {
        for (WebElement element : elements) {
            if (isVisible(element) && element.isEnabled()) return element;
        }
        return null;
    }

    private boolean isVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isInput(WebElement element) {
        String tag = element.getTagName().toLowerCase(Locale.ROOT);
        return "input".equals(tag) || "textarea".equals(tag);
    }

    private void click(WebElement element) {
        ((JavascriptExecutor) driver()).executeScript(
                "arguments[0].scrollIntoView({block:'center',inline:'center'});", element);
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", element);
        }
    }

    private String slug(String title) {
        return title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
