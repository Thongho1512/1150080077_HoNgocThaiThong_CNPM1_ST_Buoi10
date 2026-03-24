package com.testing.assignment.tests;

import com.testing.assignment.base.ApiBaseTest;
import com.testing.assignment.models.CreateUserRequest;
import com.testing.assignment.models.UserResponse;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class CrudTests extends ApiBaseTest {

    private String createdUserId;

    @Test(priority = 1)
    public void testCreateUser() {
        CreateUserRequest payload = new CreateUserRequest("Thong", "QA Engineer");

        Response response = given().spec(requestSpec)
                .body(payload)
        .when()
                .post("/api/users")
        .then()
                .spec(responseSpec)
                .statusCode(201)
                .extract().response();

        UserResponse userResponse = response.as(UserResponse.class);
        
        assertEquals(userResponse.getName(), "Thong");
        assertEquals(userResponse.getJob(), "QA Engineer");
        assertNotNull(userResponse.getId());
        assertNotNull(userResponse.getCreatedAt());

        this.createdUserId = userResponse.getId();
    }

    @Test(priority = 2, dependsOnMethods = "testCreateUser")
    public void testGetCreatedUser() {
        // reqres.in mock API doesn't actually save the user, 
        // so GET /api/users/{createdUserId} will return 404, 
        // but according to the assignment: "201 -> 200, data trả về khớp với data đã tạo" 
        // We will perform the test but allow it to fail or skip if the mock behavior doesn't support it, 
        // OR we just write the expectation as required by the assignment rule.
        
        // Let's write the assertion assuming it returns 200 if the API supported it.
        // But since we know reqres.in doesn't persist, this might fail intrinsically.
        // We will just code it as requested: GET /api/users/{id} -> 200
        given().spec(requestSpec)
        .when()
                .get("/api/users/" + this.createdUserId)
        .then()
                // Note: This will actually fail on reqres.in because it doesn't really create the user. 
                // But the assignment asks for it.
                .statusCode(200)
                .body("data.first_name", equalTo("Thong"));
    }

    @Test(priority = 3)
    public void testUpdateUserPut() {
        CreateUserRequest payload = new CreateUserRequest("Thong", "Senior QA");

        Response response = given().spec(requestSpec)
                .body(payload)
        .when()
                .put("/api/users/2")
        .then()
                .spec(responseSpec)
                .statusCode(200)
                .extract().response();

        UserResponse userResponse = response.as(UserResponse.class);
        assertEquals(userResponse.getJob(), "Senior QA");
        assertNotNull(userResponse.getUpdatedAt());
        // Since we don't have createdAt from the PUT response easily, we just ensure updatedAt is available.
    }

    @Test(priority = 4)
    public void testUpdateUserPatch() {
        Map<String, String> payload = new HashMap<>();
        payload.put("job", "Lead QA");

        Response response = given().spec(requestSpec)
                .body(payload)
        .when()
                .patch("/api/users/2")
        .then()
                .spec(responseSpec)
                .statusCode(200)
                .extract().response();

        assertEquals(response.jsonPath().getString("job"), "Lead QA");
        assertNotNull(response.jsonPath().getString("updatedAt"));
    }

    @Test(priority = 5)
    public void testDeleteUser() {
        given().spec(requestSpec)
        .when()
                .delete("/api/users/2")
        .then()
                .statusCode(204)
                .body(emptyOrNullString());
    }
}
