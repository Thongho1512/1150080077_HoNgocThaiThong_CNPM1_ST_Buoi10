package com.testing.assignment.tests;

import com.testing.assignment.base.ApiBaseTest;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertTrue;

public class PerformanceTests extends ApiBaseTest {

    @DataProvider(name = "slaData")
    public Object[][] slaData() {
        return new Object[][] {
            {"GET", "/api/users", 2000L},
            {"GET", "/api/users/2", 1500L},
            {"POST", "/api/users", 3000L},
            {"POST", "/api/login", 2000L},
            {"DELETE", "/api/users/2", 1000L}
        };
    }

    @Test(dataProvider = "slaData")
    public void testSlaMonitoring(String method, String endpoint, long maxMs) {
        long responseTime = callApiWithSla(method, endpoint, maxMs);
        System.out.println("API " + method + " " + endpoint + " took " + responseTime + "ms (Limit: " + maxMs + "ms)");
        assertTrue(responseTime <= maxMs, "Response time exceeded SLA: " + responseTime + "ms > " + maxMs + "ms");
    }

    @Step("Gọi {method} {endpoint} - SLA: {maxMs}ms")
    public long callApiWithSla(String method, String endpoint, long maxMs) {
        Response response = null;
        switch (method.toUpperCase()) {
            case "GET":
                response = given().spec(requestSpec).when().get(endpoint);
                if (endpoint.equals("/api/users")) {
                    response.then().statusCode(200).body("data.size()", greaterThanOrEqualTo(1));
                } else if (endpoint.equals("/api/users/2")) {
                    response.then().statusCode(200).body("data.id", equalTo(2));
                }
                break;
            case "POST":
                String body = endpoint.equals("/api/login") ? 
                    "{\"email\": \"eve.holt@reqres.in\", \"password\": \"cityslicka\"}" : 
                    "{\"name\": \"morpheus\", \"job\": \"leader\"}";
                    
                response = given().spec(requestSpec).body(body).when().post(endpoint);
                
                if (endpoint.equals("/api/login")) {
                    response.then().statusCode(200).body("token", notNullValue());
                } else {
                    response.then().statusCode(201).body("id", notNullValue());
                }
                break;
            case "DELETE":
                response = given().spec(requestSpec).when().delete(endpoint);
                response.then().statusCode(204);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method");
        }
        return response.getTime();
    }

    @Test
    public void testPerformanceMonitoring10Times() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long time = given().spec(requestSpec).when().get("/api/users?page=2").getTime();
            times.add(time);
        }

        long min = times.stream().min(Long::compareTo).orElse(0L);
        long max = times.stream().max(Long::compareTo).orElse(0L);
        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0.0);

        System.out.println("--- PERFORMANCE MONITORING (10 calls GET /api/users?page=2) ---");
        System.out.println("Min Response Time: " + min + " ms");
        System.out.println("Max Response Time: " + max + " ms");
        System.out.println("Avg Response Time: " + avg + " ms");
    }
}
