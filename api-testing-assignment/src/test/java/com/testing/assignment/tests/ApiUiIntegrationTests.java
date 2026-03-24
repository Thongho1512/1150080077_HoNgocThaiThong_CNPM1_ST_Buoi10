package com.testing.assignment.tests;

import com.testing.assignment.base.ApiBaseTest;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ApiUiIntegrationTests extends ApiBaseTest {

    private boolean isApiLoginSuccess = false;
    private boolean isApiAlive = false;
    private WebDriver driver;

    // --- Phần A - API Precondition → UI Verification ---
    @BeforeMethod
    public void apiPreconditionLogin() {
        System.out.println("--- Gọi API Login ---");
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "eve.holt@reqres.in");
        payload.put("password", "cityslicka");

        Response response = given().spec(requestSpec)
                .body(payload)
        .when()
                .post("/api/login");

        if (response.getStatusCode() == 200) {
            isApiLoginSuccess = true;
            System.out.println("Token: " + response.jsonPath().getString("token"));
        } else {
            System.out.println("API Login failed with status: " + response.getStatusCode());
        }
    }

    @Test
    public void testUiLoginWithApiPrecondition() {
        if (!isApiLoginSuccess) {
            throw new SkipException("Skipping UI test because API Login precondition failed.");
        }

        setupWebDriver();
        try {
            driver.get("https://www.saucedemo.com/");
            driver.findElement(By.id("user-name")).sendKeys("standard_user");
            driver.findElement(By.id("password")).sendKeys("secret_sauce");
            driver.findElement(By.id("login-button")).click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.urlContains("inventory.html"));

            assertTrue(driver.getCurrentUrl().contains("inventory"), "URL should contain 'inventory'");
            assertEquals(driver.getTitle(), "Swag Labs", "Page title should be 'Swag Labs'");
        } finally {
            cleanupWebDriver();
        }
    }

    // --- Phần B - Luồng tích hợp đầy đủ ---
    @Test
    public void testFullIntegrationFlow() {
        // Đây là bước API check
        System.out.println("--- Checking API Is Alive ---");
        Response response = given().spec(requestSpec).when().get("/api/users");
        isApiAlive = response.getStatusCode() == 200;

        if (!isApiAlive) {
            throw new SkipException("API is down! Skipping full UI flow.");
        }

        // Thực hiện luồng UI
        setupWebDriver();
        try {
            driver.get("https://www.saucedemo.com/");
            // Đây là bước UI action: Đăng nhập
            driver.findElement(By.id("user-name")).sendKeys("standard_user");
            driver.findElement(By.id("password")).sendKeys("secret_sauce");
            driver.findElement(By.id("login-button")).click();

            // Đây là bước UI action: Thêm 2 sản phẩm
            driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();
            driver.findElement(By.id("add-to-cart-sauce-labs-bike-light")).click();

            // Đây là assertion: Kiểm tra badge = 2
            String badgeText = driver.findElement(By.className("shopping_cart_badge")).getText();
            assertEquals(badgeText, "2", "Cart badge should display 2");

            // Đây là bước UI action: Vào giỏ hàng
            driver.findElement(By.className("shopping_cart_link")).click();

            // Đây là assertion: Xác nhận 2 sản phẩm trong giỏ
            int itemsCount = driver.findElements(By.className("cart_item")).size();
            assertEquals(itemsCount, 2, "There should be 2 items in the cart");
        } finally {
            cleanupWebDriver();
        }
    }

    private void setupWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Run headless for stability in environments where UI is not accessible directly or to speed up tests
        driver = new ChromeDriver(options);
    }

    private void cleanupWebDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
