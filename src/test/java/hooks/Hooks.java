package hooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import base.BaseClass;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hooks {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    @Before
    public void setup() {
        System.out.println("Launching browser");
        BaseClass.initDriver();
    }

    @After
    public void teardown(Scenario scenario) {
        if (scenario.isFailed() && BaseClass.getDriver() != null) {
            captureFailureScreenshot(scenario);
        }

        System.out.println("Closing browser");
        BaseClass.quitDriver();
    }

    private void captureFailureScreenshot(Scenario scenario) {
        try {
            Path outputDir = Paths.get("target", "screenshots");
            Files.createDirectories(outputDir);

            String safeName = scenario.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            String filename = safeName + "-" + LocalDateTime.now().format(TIMESTAMP) + ".png";
            Path target = outputDir.resolve(filename);

            byte[] screenshot = ((TakesScreenshot) BaseClass.getDriver())
                    .getScreenshotAs(OutputType.BYTES);
            Files.write(target, screenshot);

            System.out.println("[FAILURE EVIDENCE] Screenshot: " + target.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            System.err.println("[FAILURE EVIDENCE] Could not capture screenshot: " + e.getMessage());
        }
    }
}
