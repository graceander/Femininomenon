package org.cpts422.Femininomenon.App.SystemTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public class CurrencyConversionSeleniumTest {
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
     * Helper method to ensure currency is set to USD
     */
    void resetToUSD() {
        Select currencyDropdown = new Select(driver.findElement(By.xpath("//select")));
        if (!currencyDropdown.getFirstSelectedOption().getText().equals("USD")) {
            currencyDropdown.selectByVisibleText("USD");
            driver.findElement(By.xpath("//button[text()='Update Currency']")).click();
            wait.until(ExpectedConditions.textToBe(
                    By.xpath("//p[contains(text(),'Current Currency')]"),
                    "Current Currency: USD"
            ));
        }
    }

    /**
     * Tests that the currency selector displays correct default value.
     */
    @Test
    void testDefaultCurrency() {
        loginToApplication();
        resetToUSD();

        // Get the current currency text
        String currentCurrencyText = driver.findElement(
                By.xpath("//p[contains(text(),'Current Currency')]")
        ).getText();
        System.out.println("Current Currency Text: " + currentCurrencyText);

        assertEquals("Current Currency: USD", currentCurrencyText.trim(),
                "Currency display should show USD");

        // Check dropdown
        Select currencyDropdown = new Select(driver.findElement(By.xpath("//select")));
        assertEquals("USD", currencyDropdown.getFirstSelectedOption().getText().trim(),
                "Currency dropdown should default to USD");
    }

    /**
     * Tests converting currency from USD to EUR.
     */
    @Test
    void testCurrencyConversionToEUR() {
        loginToApplication();
        resetToUSD();

        // Get initial spending amount in USD
        String totalSpendingText = driver.findElement(
                By.xpath("//p[contains(text(),'Total Spending')]")
        ).getText();
        System.out.println("Initial spending text: " + totalSpendingText);
        double initialAmount = extractAmount(totalSpendingText);
        System.out.println("Initial amount: " + initialAmount);

        // Select EUR from dropdown
        Select currencyDropdown = new Select(driver.findElement(By.xpath("//select")));
        currencyDropdown.selectByVisibleText("EUR");

        // Click update button and wait for update
        driver.findElement(By.xpath("//button[text()='Update Currency']")).click();
        wait.until(ExpectedConditions.textToBe(
                By.xpath("//p[contains(text(),'Current Currency')]"),
                "Current Currency: EUR"
        ));

        // Get converted amount
        // Add a small wait to ensure the amount has updated
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String convertedSpendingText = driver.findElement(
                By.xpath("//p[contains(text(),'Total Spending')]")
        ).getText();
        System.out.println("Converted spending text: " + convertedSpendingText);
        double convertedAmount = extractAmount(convertedSpendingText);
        System.out.println("Converted amount: " + convertedAmount);

        // Calculate expected amount (USD to EUR with 1% tolerance)
        double expectedAmount = initialAmount * 0.85;
        double tolerance = expectedAmount * 0.01; // 1% tolerance

        System.out.println("Expected amount: " + expectedAmount);
        System.out.println("Tolerance: " + tolerance);

        assertTrue(Math.abs(expectedAmount - convertedAmount) <= tolerance,
                String.format("Converted amount (%.2f) should be within 1%% of expected amount (%.2f)",
                        convertedAmount, expectedAmount));

        // Verify currency indicator updated
        String currentCurrencyText = driver.findElement(
                By.xpath("//p[contains(text(),'Current Currency')]")
        ).getText();
        assertTrue(currentCurrencyText.contains("EUR"),
                "Current currency should be updated to EUR");
    }

    /**
     * Helper method to extract numeric amount from text.
     */
    private double extractAmount(String text) {
        // Remove currency symbol and any non-numeric characters except decimal point
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
