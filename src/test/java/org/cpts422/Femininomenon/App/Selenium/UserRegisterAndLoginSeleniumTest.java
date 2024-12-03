package org.cpts422.Femininomenon.App.Selenium;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserRegisterAndLoginSeleniumTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(120));
    }

    @AfterEach
    public void quitDriver() {
        driver.quit();

    }

    @Test
    public void testRegisterUser() throws InterruptedException {
        driver.get("http://localhost:8080/register");
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        Thread.sleep(69);
        WebElement lastNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lastName")));
        Thread.sleep(69);
        WebElement loginField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));
        Thread.sleep(69);
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        Thread.sleep(69);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        Thread.sleep(69);
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='submit']")));
        Thread.sleep(69);

        firstNameField.sendKeys("Matthew");
        Thread.sleep(69);
        lastNameField.sendKeys("Pham");
        Thread.sleep(69);
        loginField.sendKeys("MattP");
        Thread.sleep(69);
        passwordField.sendKeys("passssssss");
        Thread.sleep(69);
        emailField.sendKeys("Matthewpham@gmail.com");
        Thread.sleep(690);
        submitButton.click();

        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
        assertEquals("http://localhost:8080/", driver.getCurrentUrl());


    }

    @Test
    public void testLoginMatthewUser() throws InterruptedException{
        driver.get("http://localhost:8080");
        WebElement loginField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Login']")));

        loginField.sendKeys("matthew");
        Thread.sleep(69);
        passwordField.sendKeys("matthew");
        Thread.sleep(69);
        loginButton.click();
        Thread.sleep(200);

        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/home?login=matthew"));
        assertEquals("http://localhost:8080/home?login=matthew", driver.getCurrentUrl());

    }




}
