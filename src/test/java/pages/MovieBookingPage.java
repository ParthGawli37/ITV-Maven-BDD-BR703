package pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import base.BaseClass;
import utils.LoggerUtils;

/**
 * District.in movie booking flow.
 *
 * Current movie, theatre, date, showtime and seat data are discovered at
 * runtime so the test does not depend on a stale movie title.
 */
public class MovieBookingPage extends BaseClass {

    private static final Pattern DATE_TEXT = Pattern.compile(
            "(?i).*\\b(mon|tue|wed|thu|fri|sat|sun)\\b.*\\d{1,2}.*");
    private static final Pattern TIME_TEXT = Pattern.compile(
            "(?i).*\\b\\d{1,2}:\\d{2}\\s*(am|pm)\\b.*");
    private static final Pattern UNAVAILABLE = Pattern.compile(
            "(?i).*(unavailable|occupied|booked|blocked|disabled|sold|not.?available).*");

    private String selectedMovie;
    private String selectedTheatre;
    private String selectedDate;
    private String selectedShowtime;
    private WebElement selectedShowtimeElement;

    public MovieBookingPage() {
        super();
    }

    private By currentMovieLinks() {
        return By.xpath(
                "//*[self::h1 or self::h2 or self::h3 or self::h4][contains(normalize-space(.),'Only in Theatres')]"
                + "/following::a[contains(@href,'/movies/') and contains(@href,'-movie-tickets-in-')]");
    }

    public void assertCurrentMoviesAvailable() {
        int count = visibleElements(currentMovieLinks()).size();
        Assert.assertTrue(count >= 3,
                "Expected at least 3 current movies in 'Only in Theatres', but found " + count);
        LoggerUtils.info("Current movie cards detected: " + count);
    }

    public void selectCurrentTopMovie() {
        List<WebElement> links = visibleElements(currentMovieLinks());
        Assert.assertFalse(links.isEmpty(), "No current movie cards were found in 'Only in Theatres'");

        WebElement movie = links.get(0);
        selectedMovie = cleanText(movie.getText());
        if (selectedMovie.isBlank()) {
            selectedMovie = movie.getAttribute("aria-label");
        }

        clickSafely(movie);
        waitForPageLoad();
        LoggerUtils.info("Selected current movie: " + selectedMovie);
    }

    public void selectFirstAvailableDate() {
        List<WebElement> dateElements = new ArrayList<>();
        List<WebElement> candidates = driver.findElements(
                By.xpath("//*[self::button or @role='button' or self::a]"));

        for (WebElement element : candidates) {
            if (!isUsable(element)) continue;
            String text = cleanText(element.getText());
            if (DATE_TEXT.matcher(text).matches()) {
                dateElements.add(element);
            }
        }

        Assert.assertFalse(dateElements.isEmpty(), "No selectable movie dates were found");
        WebElement date = dateElements.get(0);
        selectedDate = cleanText(date.getText());
        clickSafely(date);
        waitForShowtimes();
        LoggerUtils.info("Selected date: " + selectedDate);
    }

    /**
     * Finds a theatre that has a usable showtime and remembers that exact
     * showtime so the later showtime step cannot accidentally switch theatres.
     */
    public void selectFirstTheatreWithAvailableShowtime() {
        List<WebElement> showtimes = findAvailableShowtimes();
        Assert.assertFalse(showtimes.isEmpty(),
                "No available theatre/showtime was found for the selected movie/date");

        selectedShowtimeElement = showtimes.get(0);
        selectedTheatre = inferTheatreName(selectedShowtimeElement);
        LoggerUtils.info("Selected theatre candidate: " + selectedTheatre);
    }

    public void selectFirstAvailableShowtime() {
        if (selectedShowtimeElement == null || !isUsable(selectedShowtimeElement)) {
            List<WebElement> showtimes = findAvailableShowtimes();
            Assert.assertFalse(showtimes.isEmpty(), "No available showtimes were found");
            selectedShowtimeElement = showtimes.stream()
                    .min(Comparator.comparing(this::timeMinutes))
                    .orElse(showtimes.get(0));
        }

        selectedShowtime = cleanText(selectedShowtimeElement.getText());
        if (selectedTheatre == null || selectedTheatre.isBlank()) {
            selectedTheatre = inferTheatreName(selectedShowtimeElement);
        }

        clickSafely(selectedShowtimeElement);
        waitForSeatMap();
        LoggerUtils.info("Selected showtime: " + selectedShowtime + " at " + selectedTheatre);
    }

    public void assertAtLeastTwoSeatsAvailable() {
        List<WebElement> seats = findAvailableSeats();
        Assert.assertTrue(seats.size() >= 2,
                "Expected at least 2 available seats, but found " + seats.size());
        LoggerUtils.info("Available seats detected: " + seats.size());
    }

    /**
     * Picks a pair of adjacent available seats closest to the visual centre of
     * the seat map. This is a heuristic, not a claim that District marks a
     * particular seat as objectively 'best'.
     */
    public void selectBestAdjacentSeatsForTwo() {
        List<WebElement> seats = findAvailableSeats();
        Assert.assertTrue(seats.size() >= 2,
                "Cannot select 2 seats because fewer than 2 available seats were detected");

        List<WebElement> bestPair = findBestAdjacentPair(seats);
        Assert.assertEquals(bestPair.size(), 2,
                "Could not find a pair of adjacent available seats");

        for (WebElement seat : bestPair) {
            clickSafely(seat);
        }
        LoggerUtils.info("Selected best adjacent seats for 2 people");
    }

    public void clickProceedOrCheckIn() {
        By proceed = By.xpath(
                "//*[self::button or @role='button' or self::a]"
                + "[contains(translate(normalize-space(.),"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'proceed')"
                + " or contains(translate(normalize-space(.),"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'check in')]");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement button = wait.until(d -> {
            for (WebElement element : d.findElements(proceed)) {
                if (isUsable(element)) return element;
            }
            return null;
        });

        clickSafely(button);
        waitForPageLoad();
        LoggerUtils.info("Clicked Proceed/Check In button");
    }

    public void assertBookingFlowContinued() {
        String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
        String pageText = cleanText(driver.findElement(By.tagName("body")).getText()).toLowerCase(Locale.ROOT);

        boolean progressed = url.contains("checkout")
                || url.contains("booking")
                || url.contains("payment")
                || pageText.contains("checkout")
                || pageText.contains("payment")
                || pageText.contains("contact details")
                || pageText.contains("review booking")
                || pageText.contains("confirm");

        Assert.assertTrue(progressed,
                "Proceed/Check In was clicked, but the next booking step could not be detected. URL=" + url);
    }

    private List<WebElement> findAvailableShowtimes() {
        List<WebElement> result = new ArrayList<>();
        List<WebElement> candidates = driver.findElements(
                By.xpath("//*[self::button or @role='button' or self::a]"));

        for (WebElement element : candidates) {
            if (!isUsable(element)) continue;
            String text = cleanText(element.getText());
            if (TIME_TEXT.matcher(text).matches() && !UNAVAILABLE.matcher(text).matches()) {
                result.add(element);
            }
        }
        return result;
    }

    private List<WebElement> findAvailableSeats() {
        List<WebElement> candidates = driver.findElements(By.cssSelector(
                "[data-testid*='seat'], [data-seat], [data-seat-status], "
                + "[aria-label*='seat' i], [aria-label*='available' i], [class*='seat']"));
        List<WebElement> result = new ArrayList<>();

        for (WebElement seat : candidates) {
            if (!isUsable(seat)) continue;

            String metadata = (cleanText(seat.getAttribute("aria-label")) + " "
                    + cleanText(seat.getAttribute("class")) + " "
                    + cleanText(seat.getAttribute("data-seat-status"))).toLowerCase(Locale.ROOT);

            if (UNAVAILABLE.matcher(metadata).matches()) continue;

            String tag = seat.getTagName().toLowerCase(Locale.ROOT);
            if ("svg".equals(tag) || "path".equals(tag)) continue;

            if (seat.getRect().getWidth() < 5 || seat.getRect().getHeight() < 5) continue;
            result.add(seat);
        }
        return result;
    }

    private List<WebElement> findBestAdjacentPair(List<WebElement> seats) {
        List<WebElement> best = new ArrayList<>();
        double viewportCenterX = driver.manage().window().getSize().getWidth() / 2.0;
        double viewportCenterY = driver.manage().window().getSize().getHeight() / 2.0;
        double bestScore = Double.MAX_VALUE;

        for (WebElement first : seats) {
            for (WebElement second : seats) {
                if (first.equals(second)) continue;

                double yGap = Math.abs(first.getRect().getY() - second.getRect().getY());
                double firstRight = first.getRect().getX() + first.getRect().getWidth();
                double secondLeft = second.getRect().getX();
                double gap = Math.abs(secondLeft - firstRight);

                if (yGap > 15 || gap > 80) continue;

                double pairCenterX = (first.getRect().getX() + second.getRect().getX()) / 2.0;
                double pairCenterY = (first.getRect().getY() + second.getRect().getY()) / 2.0;
                double score = (gap * 4)
                        + Math.abs(pairCenterX - viewportCenterX)
                        + Math.abs(pairCenterY - viewportCenterY) * 0.25;

                if (score < bestScore) {
                    bestScore = score;
                    best = List.of(first, second);
                }
            }
        }
        return best;
    }

    private int timeMinutes(WebElement element) {
        String text = cleanText(element.getText()).toUpperCase(Locale.ROOT);
        java.util.regex.Matcher matcher = Pattern.compile(
                "(\\d{1,2}):(\\d{2})\\s*(AM|PM)").matcher(text);
        if (!matcher.find()) return Integer.MAX_VALUE;

        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        String meridiem = matcher.group(3);
        if ("AM".equals(meridiem) && hour == 12) hour = 0;
        if ("PM".equals(meridiem) && hour != 12) hour += 12;
        return hour * 60 + minute;
    }

    private String inferTheatreName(WebElement showtime) {
        String[] selectors = {
                "ancestor::*[self::div or self::section][.//*[contains(.,'Cinema') or contains(.,'PVR') or contains(.,'INOX') or contains(.,'Cinepolis')]][1]",
                "ancestor::*[self::div or self::section][1]"
        };

        for (String selector : selectors) {
            try {
                List<WebElement> ancestors = showtime.findElements(By.xpath(selector));
                if (!ancestors.isEmpty()) {
                    String text = cleanText(ancestors.get(0).getText());
                    if (!text.isBlank()) return text.split("\\n")[0];
                }
            } catch (Exception ignored) {
                // Try the next ancestor strategy.
            }
        }
        return "First available theatre";
    }

    private void waitForShowtimes() {
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(d -> !findAvailableShowtimes().isEmpty());
    }

    private void waitForSeatMap() {
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> !findAvailableSeats().isEmpty());
    }

    private List<WebElement> visibleElements(By locator) {
        List<WebElement> result = new ArrayList<>();
        for (WebElement element : driver.findElements(locator)) {
            if (isUsable(element)) result.add(element);
        }
        return result;
    }

    private boolean isUsable(WebElement element) {
        try {
            return element.isDisplayed() && element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    private void clickSafely(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center',inline:'center'});", element);
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private String cleanText(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }
}