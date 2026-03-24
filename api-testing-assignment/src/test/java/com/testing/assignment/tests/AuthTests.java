package com.testing.assignment.tests;

import com.testing.assignment.base.ApiBaseTest;
import io.restassured.response.ValidatableResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthTests extends ApiBaseTest {

    @Test
    public void testLoginSuccess() {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "eve.holt@reqres.in");
        payload.put("password", "cityslicka");

        given().spec(requestSpec)
                .body(payload)
        .when()
                .post("/api/login")
        .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("token", not(emptyOrNullString()));
    }

    @Test
    public void testLoginMissingPassword() {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "eve.holt@reqres.in");

        given().spec(requestSpec)
                .body(payload)
        .when()
                .post("/api/login")
        .then()
                .statusCode(400)
                .body("error", equalTo("Missing password"));
    }

    @Test
    public void testLoginMissingEmail() {
        Map<String, String> payload = new HashMap<>();
        payload.put("password", "cityslicka");

        given().spec(requestSpec)
                .body(payload)
        .when()
                .post("/api/login")
        .then()
                .statusCode(400)
                .body("error", equalTo("Missing email or username"));
    }

    @Test
    public void testRegisterSuccess() {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "eve.holt@reqres.in");
        payload.put("password", "pistol");

        given().spec(requestSpec)
                .body(payload)
        .when()
                .post("/api/register")
        .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("id", notNullValue())
                .body("token", not(emptyOrNullString()));
    }

    @Test
    public void testRegisterMissingPassword() {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "eve.holt@reqres.in");

        given().spec(requestSpec)
                .body(payload)
        .when()
                .post("/api/register")
        .then()
                .statusCode(400)
                .body("error", equalTo("Missing password"));
    }

    @DataProvider(name = "loginScenarios")
    public Object[][] loginScenarios() {
        return new Object[][] {
            {"eve.holt@reqres.in", "cityslicka", 200, null},
            {"eve.holt@reqres.in", "", 400, "Missing password"},
            {"", "cityslicka", 400, "Missing email or username"},
            {"notexist@reqres.in", "wrongpass", 400, "user not found"},
            {"invalid-email", "pass123", 400, "user not found"}
        };
    }

    @Test(dataProvider = "loginScenarios")
    public void testLoginScenarios(String email, String password, int expectedStatus, String expectedError) {
        Map<String, String> body = new HashMap<>();
        if (email != null && !email.isEmpty()) body.put("email", email);
        if (password != null && !password.isEmpty()) body.put("password", password);

        ValidatableResponse response = given().spec(requestSpec).body(body)
                .when().post("/api/login").then().statusCode(expectedStatus);

        if (expectedError != null) {
            response.body("error", containsString(expectedError));
        } else {
            response.body("token", not(emptyOrNullString())); // implicit assert for success
        }
    }
}
