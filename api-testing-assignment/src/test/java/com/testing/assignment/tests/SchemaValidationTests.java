package com.testing.assignment.tests;

import com.testing.assignment.base.ApiBaseTest;
import com.testing.assignment.models.CreateUserRequest;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

// The assignment says additionalProperties: false must cause failure 
// if backend adds fields. We simulate this by validating endpoints that might have new fields,
// or just standard validation.
public class SchemaValidationTests extends ApiBaseTest {

    @Test
    public void testUserListSchema() {
        given().spec(requestSpec)
        .when()
                .get("/api/users?page=1")
        .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/user-list-schema.json"));
    }

    @Test
    public void testSingleUserSchema() {
        given().spec(requestSpec)
        .when()
                .get("/api/users/2")
        .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/user-schema.json"));
    }

    @Test
    public void testCreateUserSchema() {
        CreateUserRequest payload = new CreateUserRequest("QAMaster", "Testing");
        given().spec(requestSpec)
                .body(payload)
        .when()
                .post("/api/users")
        .then()
                .statusCode(201)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/create-user-schema.json"));
    }
}
