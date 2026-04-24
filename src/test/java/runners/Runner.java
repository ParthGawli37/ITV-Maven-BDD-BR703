package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions", "hooks"},
    plugin = {"pretty", "html:target/cucumber-report.html"},
    tags = "@SmokeTest or @RegressionTest"
)
public class Runner extends AbstractTestNGCucumberTests {
}