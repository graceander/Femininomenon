package org.cpts422.Femininomenon.App.SystemTests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

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
     * Creates a large transaction that should trigger an inbox message.
     */
    void createLargeTransaction() {
        // Find and click "Add Transaction" button
        List<WebElement> mainButtons = driver.findElements(By.tagName("button"));
        for (WebElement button : mainButtons) {
            if (button.getText().equals("Add Transaction")) {
                button.click();
                break;
            }
        }

        // Fill out transaction form with a large amount
        WebElement amount = driver.findElement(By.id("amount"));
        amount.sendKeys("5000"); // Large amount to trigger alert

        WebElement category = driver.findElement(By.id("category"));
        Select selectCategory = new Select(category);
        selectCategory.selectByVisibleText("Groceries");

        WebElement description = driver.findElement(By.id("description"));
        description.sendKeys("Large grocery purchase");

        WebElement type = driver.findElement(By.id("type"));
        Select selectType = new Select(type);
        selectType.selectByVisibleText("Expense");

        WebElement account = driver.findElement(By.id("account"));
        Select selectAccount = new Select(account);
        selectAccount.selectByVisibleText("Checking");

        // Submit the transaction
        WebElement addButton = driver.findElement(By.tagName("button"));
        addButton.click();
    }

    /**
     * Tests that the expected messages appear after creating a large transaction.
     */
    @Test
    void testInboxMessageExists() {
        loginToApplication();
        createLargeTransaction();
        navigateToInbox();

        // Get all rows from the table
        List<WebElement> rows = driver.findElements(By.tagName("tr"));

        // Skip header row
        for (int i = 1; i < rows.size(); i++) {
            WebElement row = rows.get(i);
            // Get cells for this row
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() > 0) {
                String messageText = cells.get(0).getText(); // Message column
                String status = cells.get(2).getText(); // Status column

                if (i == 1) {
                    assertEquals("Alert: You have expenses ($5000.00) but no income recorded this month.",
                            messageText, "First message should indicate no income");
                    assertEquals("Unread", status, "First message should be unread");
                } else if (i == 2) {
                    assertEquals("Alert: You have a large individual expense of $5000.00 with no income recorded this month.",
                            messageText, "Second message should indicate large expense");
                    assertEquals("Unread", status, "Second message should be unread");
                }
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

    /**
     * Tests marking a single message as read.
     */
    @Test
    void testMarkMessageAsRead() {
        loginToApplication();
        createLargeTransaction();
        navigateToInbox();

        // Find all buttons and click the first "Mark as Read" button
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        for (WebElement button : buttons) {
            if (button.getText().equals("Mark as Read")) {
                button.click();
                break;
            }
        }

        // Allow time for the status to update
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));

        // Get the rows again after clicking the button
        List<WebElement> rows = driver.findElements(By.tagName("tr"));
        // Skip header row, check first message row
        List<WebElement> cells = rows.get(1).findElements(By.tagName("td"));

        // Verify the status changed to read
        assertEquals("Read", cells.get(2).getText(),
                "Message status should change to Read");
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}