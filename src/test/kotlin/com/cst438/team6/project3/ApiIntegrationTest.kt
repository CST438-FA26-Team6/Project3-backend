package com.cst438.team6.project3

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(TestcontainersConfiguration::class)
class ApiIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
    }

    @Test
    fun `protected endpoint without authentication returns 401`() {
        given()
            .`when`()
            .get("/")
            .then()
            .statusCode(401)
    }

    @Test
    fun `CORS preflight from Expo web client succeeds`() {
        given()
            .header("Origin", "http://localhost:8081")
            .header("Access-Control-Request-Method", "GET")
            .`when`()
            .options("/")
            .then()
            .statusCode(200)
            .header(
                "Access-Control-Allow-Origin",
                equalTo("http://localhost:8081")
            )
    }
}