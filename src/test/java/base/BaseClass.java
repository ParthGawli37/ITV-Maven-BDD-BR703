package base;

import java.time.Duration;

import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.ConfigReader;

public class BaseClass {

    protected static WebDriver driver;
    private static final int ACTION_RETRIES = 2;

    public static void initDriver() {
        String browser = System.getProperty("browser", ConfigReader.get("browser"));
        String headless = System.getProperty("headless", ConfigReader.get("headless"));

        if (browser == null || browser.isBlank()) {
            browser = "chrome";
        }

        boolean isHeadless = Boolean.parseBoolean(headless == null ? "false" : headless);

        switch (browser.trim().toLowerCase()) {
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (isHeadless) options.addArguments("-headless");
                driver = new FirefoxDriver(options);
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                if (isHeadless) options.addArguments("--headless=new");
                driver = new EdgeDriver(options);
            }
            default -> {
                ChromeOptions options = new ChromeOptions();
                if (isHeadless) options.addArguments("--headless=new");
                options.addArguments("--disable-notifications");
                driver = new ChromeDriver(options);
            }
        }

        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    /** Retries only transient Selenium interaction failures, not assertions. */
    public void elementClick(WebElement element) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= ACTION_RETRIES; attempt++) {
            try {
                new WebDriverWait(driver, Duration.ofSeconds(15))
                        .until(ExpectedConditions.elementToBeClickable(element)).click();
                return;
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                lastFailure = e;
                pause(300L * attempt);
            }
        }
        throw lastFailure;
    }

    /** Retries only transient stale-element failures; validation/assertions are untouched. */
    public void enterText(WebElement element, String text) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= ACTION_RETRIES; attempt++) {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                WebElement el = wait.until(ExpectedConditions.visibilityOf(element));
                el.clear();
                el.sendKeys(text);
                return;
            } catch (StaleElementReferenceException e) {
                lastFailure = e;
                pause(300L * attempt);
            }
        }
        throw lastFailure;
    }

    public void waitForPageLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                d -> ((org.openqa.selenium.JavascriptExecutor) d)
                        .executeScript("return document.readyState").equals("complete"));
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry wait was interrupted", e);
        }
    }
}
