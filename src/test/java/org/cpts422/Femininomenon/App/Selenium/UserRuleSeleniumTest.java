package org.cpts422.Femininomenon.App.Selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class UserRuleSeleniumTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void quitDriver() {
        driver.quit();
    }

    @Test
    public void testAddUserRule() {
        driver.get("http://localhost:8080/user/rules/view?userLogin=matthew");
        WebElement addRuleButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("addRuleBtn")));
        addRuleButton.click();
        WebElement categorySelect = driver.findElement(By.id("category"));
        categorySelect.sendKeys("UTILITIES");
        WebElement limitAmount = driver.findElement(By.id("limitAmount"));
        limitAmount.sendKeys("100.00");
        WebElement frequency = driver.findElement(By.id("frequency"));
        frequency.sendKeys("WEEKLY");
        WebElement ruleTypeSelect = driver.findElement(By.id("ruleType"));
        ruleTypeSelect.sendKeys("MAXIMUM_SPENDING");
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
        wait.until(ExpectedConditions.urlContains("/home"));
        driver.get("http://localhost:8080/user/rules/view?userLogin=matthew");

        WebElement ruleCategory = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='UTILITIES']")));
        assertNotNull(ruleCategory);
    }

}
