package utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed Cucumber/TestNG scenario once.
 *
 * A retry is intended for transient browser/network/UI failures, not to hide
 * deterministic assertion failures. Final failure is still reported by TestNG.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRIES = 1;
    private int retryCount = 0;

    @Override
    public synchronized boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            System.out.println("[RETRY] Retrying failed scenario: "
                    + result.getMethod().getMethodName()
                    + " | attempt " + (retryCount + 1) + " of " + (MAX_RETRIES + 1));
            return true;
        }
        return false;
    }
}
