package org.cpts422.Femininomenon.App.Selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserSimpleTransactionSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterEach
    public void quitDriver() {
        driver.quit();
    }

    @Test
    public void testEditTransaction() throws InterruptedException{
        driver.get("http://localhost:8080/editTransaction?id=7&login=matthew");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editForm")));

        WebElement dateField = driver.findElement(By.name("date"));
        dateField.clear();
        dateField.sendKeys("2024-12-25T10:00:00");

        WebElement amountField = driver.findElement(By.name("amount"));
        amountField.clear();
        amountField.sendKeys("123.69");

        WebElement categoryField = driver.findElement(By.name("category"));
        categoryField.clear();
        categoryField.sendKeys("GROCERIES");

        WebElement descriptionField = driver.findElement(By.name("description"));
        descriptionField.clear();
        descriptionField.sendKeys("Weekly grocer");

        WebElement typeField = driver.findElement(By.name("type"));
        typeField.sendKeys("EXPENSE");

        WebElement accountField = driver.findElement(By.name("account"));
        accountField.clear();
        accountField.sendKeys("Credit Card");
        WebElement submitButton = driver.findElement(By.xpath("//button[contains(text(),'Save Changes')]"));
        Thread.sleep(1000);
        submitButton.click();

        Thread.sleep(1000);
        wait.until(ExpectedConditions.urlContains("/home"));
        WebElement transactionAmount = driver.findElement(By.xpath("//td[contains(text(),'123.69')]"));
        assertEquals("123.69", transactionAmount.getText());
    }


    // again once it's deleted you have to restart the application so it can find the id
    @Test
    public void testDeleteTransaction() throws InterruptedException {
        driver.get("http://localhost:8080/editTransaction?id=7&login=matthew");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editForm")));
        WebElement deleteButton = driver.findElement(By.xpath("//form[@action='/deleteTransaction']//button"));
        deleteButton.click();
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
        Thread.sleep(5000);
        wait.until(ExpectedConditions.urlContains("/home"));
        boolean isTransactionDeleted = driver.findElements(By.xpath("//td[contains(text(),'123.69')]")).isEmpty();
        assertTrue(isTransactionDeleted);
    }

    @Test
    public void testAddTransaction() throws InterruptedException {
        driver.get("http://localhost:8080/addTransaction?login=matthew");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'MEOWWW a new Transaction?')]")));

        // Fill out the transaction form
        WebElement amountField = driver.findElement(By.id("amount"));
        amountField.clear();
        amountField.sendKeys("200.69");

        WebElement categoryField = driver.findElement(By.id("category"));
        categoryField.sendKeys("Groceries");

        WebElement descriptionField = driver.findElement(By.id("description"));
        descriptionField.clear();
        descriptionField.sendKeys("Weeklys shopping");
        WebElement typeField = driver.findElement(By.id("type"));
        typeField.sendKeys("EXPENSE");
        WebElement accountField = driver.findElement(By.id("account"));
        accountField.sendKeys("Checking");
        WebElement submitButton = driver.findElement(By.xpath("//button[contains(text(),'Add Transaction')]"));
        Thread.sleep(3000);
        submitButton.click();
        wait.until(ExpectedConditions.urlContains("/home"));
        WebElement transactionAmount = driver.findElement(By.xpath("//td[contains(text(),'200.69')]"));
        Thread.sleep(3000);
        assertEquals("200.69", transactionAmount.getText());
    }








}
