package com.testing.assignment.tests;

import com.testing.assignment.base.ApiBaseTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetTests extends ApiBaseTest {

    @Test
    public void testGetUsersPage1() {
        given().spec(requestSpec)
                .queryParam("page", 1)
        .when()
                .get("/api/users")
        .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("page", equalTo(1))
                .body("total_pages", greaterThan(0))
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    public void testGetUsersPage2() {
        given().spec(requestSpec)
                .queryParam("page", 2)
        .when()
                .get("/api/users")
        .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("page", equalTo(2))
                .body("data.id", everyItem(notNullValue()))
                .body("data.email", everyItem(notNullValue()))
                .body("data.first_name", everyItem(notNullValue()))
                .body("data.last_name", everyItem(notNullValue()))
                .body("data.avatar", everyItem(notNullValue()));
    }

    @Test
    public void testGetUserById3() {
        given().spec(requestSpec)
        .when()
                .get("/api/users/3")
        .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("data.id", equalTo(3))
                .body("data.email", endsWith("@reqres.in"))
                .body("data.first_name", not(emptyOrNullString()));
    }

    @Test
    public void testGetUserById9999() {
        given().spec(requestSpec)
        .when()
                .get("/api/users/9999")
        .then()
                // Not using responseSpec here as it expects ContentType.JSON, 
                // but 404 from reqres.in returns JSON `{}`. Let's still use it but catch exceptions if needed.
                .spec(responseSpec)
                .statusCode(404)
                .body("isEmpty()", is(true));
    }
}
