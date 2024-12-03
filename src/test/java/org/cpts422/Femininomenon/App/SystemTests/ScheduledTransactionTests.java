package org.cpts422.Femininomenon.App.SystemTests;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.time.Duration;

public class ScheduledTransactionTests {
    WebDriver driver;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
    }

    void loginToApplication() {
        // login
        driver.get("http://localhost:8080/");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        String loginInfo = "briana";
        WebElement pageUserName = driver.findElement(By.id("login"));
        WebElement pagePassword = driver.findElement(By.id("password"));
        pageUserName.sendKeys(loginInfo);
        pagePassword.sendKeys(loginInfo);
        driver.findElement(By.tagName("button")).click();
    }

    void navigateToViewScheduledTransactions() {
        // navigate to ScheduledTransaction page
        List<WebElement> mainButtons = driver.findElements(By.tagName("button"));
        for (WebElement button : mainButtons) {
            if (button.getText().equals("View Scheduled Transactions")) {
                button.click();
                break;
            }
        }
    }

    @Test
    void createNormalScheduledTransactionTest() {
        loginToApplication();
        navigateToViewScheduledTransactions();

        WebElement createButton = driver.findElement(By.tagName("button"));
        createButton.click();

        // complete the form
        WebElement amount = driver.findElement(By.id("amount"));
        amount.sendKeys("100");

        WebElement frequency = driver.findElement(By.id("frequency"));
        Select selectFreq = new Select(frequency);
        selectFreq.selectByVisibleText("Weekly");

        WebElement category = driver.findElement(By.id("category"));
        Select selectCat = new Select(category);
        selectCat.selectByVisibleText("Utilities");

        WebElement initTransactionDate = driver.findElement(By.id("recentPayment"));
        String startDate = "12052024"; // December 5th, 2024
        initTransactionDate.sendKeys(startDate);

        WebElement description = driver.findElement(By.id("description"));
        description.sendKeys("Hot Pockets");

        WebElement type = driver.findElement(By.id("type"));
        Select selectType = new Select(type);
        selectType.selectByVisibleText("Expense");

        WebElement account = driver.findElement(By.id("account"));
        Select selectAcc = new Select(account);
        selectAcc.selectByVisibleText("Checking");

        WebElement addButton = driver.findElement(By.tagName("button"));
        addButton.click();

        // validate information
        List<WebElement> tableContents = driver.findElements(By.tagName("td"));

        assertEquals("Weekly", tableContents.get(1).getText()); // frequency
        assertEquals("2025-12-11T00:00", tableContents.get(2).getText()); // most recent payment
        assertEquals("100.0", tableContents.get(3).getText()); // amount
        assertEquals("UTILITIES" , tableContents.get(4).getText()); // category
        assertEquals("Scheduled Transaction: Hot Pockets", tableContents.get(5).getText()); // description
        assertEquals("EXPENSE", tableContents.get(6).getText()); // type
        assertEquals("Checking", tableContents.get(7).getText()); // account

        // validate all transactions are made
        // return to home screen
        List<WebElement> stButtons = driver.findElements(By.tagName("button"));
        for (WebElement button : stButtons) {
            if (button.getText().equals("Go Home")) {
                button.click();
                break;
            }
        }

        // search transaction table for newly created transactions
        List<WebElement> transactionHistory = driver.findElements(By.tagName("tr"));
        int newTransactionCount = 52;
        for (int i = transactionHistory.size() - 1; i >= 0; i--) {
            WebElement transaction = transactionHistory.get(i);
            List<WebElement> transactionData = transaction.findElements(By.tagName("td"));
            assertEquals("Scheduled Transaction: Hot Pockets", transactionData.get(4).getText());
            newTransactionCount--;
            if (newTransactionCount == 0) { break; }
        }
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}
