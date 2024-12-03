package org.cpts422.Femininomenon.App.SystemTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import java.util.List;
import java.time.Duration;

public class SpendingOverviewSeleniumTest {
    WebDriver driver;
    WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    void loginToApplication() {
        driver.get("http://localhost:8080/");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        String loginInfo = "grace";
        WebElement pageUserName = driver.findElement(By.id("login"));
        WebElement pagePassword = driver.findElement(By.id("password"));
        pageUserName.sendKeys(loginInfo);
        pagePassword.sendKeys(loginInfo);
        driver.findElement(By.tagName("button")).click();
    }

    /**
     * Tests changing the period selection in spending overview.
     */
    @Test
    void testPeriodSelection() {
        loginToApplication();

        String[] periods = {"Overall", "Day", "Week", "Month", "Year"};

        for (String period : periods) {
            // Find and select period
            WebElement periodDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.name("period")));
            Select periodSelect = new Select(periodDropdown);
            periodSelect.selectByVisibleText(period);

            // Wait for page to reload and verify period was selected
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//p[contains(text(),'Total Spending')]")));

            // Re-locate the dropdown after page reload
            periodDropdown = driver.findElement(By.name("period"));
            periodSelect = new Select(periodDropdown);
            assertEquals(period, periodSelect.getFirstSelectedOption().getText(),
                    "Period should be set to " + period);
        }
    }

    /**
     * Tests displaying of zero spending message.
     */
    @Test
    void testZeroSpending() {
        loginToApplication();

        // Select Day view (assuming current day has no transactions)
        WebElement periodDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.name("period")));
        Select periodSelect = new Select(periodDropdown);
        periodSelect.selectByVisibleText("Day");

        // Wait for page update
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//p[contains(text(),'Total Spending')]")));

        // Check for either zero spending message or spending chart
        List<WebElement> noExpensesMessages = driver.findElements(
                By.xpath("//div[contains(text(),'No expenses logged for this period')]"));
        List<WebElement> charts = driver.findElements(By.id("spendingChart"));

        if (extractAmount(driver.findElement(By.xpath("//p[contains(text(),'Total Spending')]")).getText()) == 0) {
            assertTrue(!noExpensesMessages.isEmpty(), "Zero spending message should be shown");
            assertTrue(charts.isEmpty(), "Chart should not be shown for zero spending");
        } else {
            assertTrue(!charts.isEmpty(), "Chart should be shown for non-zero spending");
        }
    }

    private double extractAmount(String text) {
        String numericText = text.replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(numericText);
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse amount from text: " + text);
            throw e;
        }
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}