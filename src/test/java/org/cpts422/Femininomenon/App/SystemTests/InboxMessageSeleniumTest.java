package org.cpts422.Femininomenon.App.SystemTests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InboxMessageSeleniumTest {
    WebDriver driver;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
    }

    /**
     * Helper method to log into the application
     */
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
     * Helper method to navigate to the Inbox page
     */
    void navigateToInbox() {
        List<WebElement> mainButtons = driver.findElements(By.tagName("button"));
        for (WebElement button : mainButtons) {
            if (button.getText().equals("View Inbox")) {
                button.click();
                break;
            }
        }
    }

    /**
     * Tests basic inbox access and verifies the page structure
     */
    @Test
    void testInboxAccess() {
        // Login and navigate to inbox
        loginToApplication();
        navigateToInbox();

        // Verify inbox page elements
        List<WebElement> headers = driver.findElements(By.tagName("th"));
        assertEquals("Message", headers.get(0).getText());
        assertEquals("Timestamp", headers.get(1).getText());
        assertEquals("Status", headers.get(2).getText());

        // Verify "Go Home" button exists
        boolean foundHomeButton = false;
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        for (WebElement button : buttons) {
            if (button.getText().equals("Go Home")) {
                foundHomeButton = true;
                break;
            }
        }
        assertTrue(foundHomeButton, "Go Home button should be present");
    }

    /**
     * Tests navigation back to home page from inbox
     */
    @Test
    void testNavigateHomeFromInbox() {
        loginToApplication();
        navigateToInbox();

        // Click Go Home button
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        for (WebElement button : buttons) {
            if (button.getText().equals("Go Home")) {
                button.click();
                break;
            }
        }

        // Verify we're back on the main page by checking for transaction table
        List<WebElement> headers = driver.findElements(By.tagName("th"));
        assertEquals("Date", headers.get(1).getText());
        assertEquals("Amount (USD)", headers.get(2).getText());
        assertEquals("Category", headers.get(3).getText());
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}