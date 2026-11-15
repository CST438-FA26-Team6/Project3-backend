package com.cst438.team6.project3.library

import com.cst438.team6.project3.TestcontainersConfiguration
import com.cst438.team6.project3.service.AppTokenService
import com.cst438.team6.project3.title.MediaType
import com.cst438.team6.project3.title.Title
import com.cst438.team6.project3.title.TitleRepository
import com.cst438.team6.project3.user.User
import com.cst438.team6.project3.user.UserRepository
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(TestcontainersConfiguration::class)
class LibraryControllerIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var libraryEntryRepository: LibraryEntryRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var titleRepository: TitleRepository

    @Autowired
    private lateinit var appTokenService: AppTokenService

    private lateinit var user: User
    private lateinit var anime: Title
    private lateinit var token: String

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port

        libraryEntryRepository.deleteAll()
        userRepository.deleteAll()
        titleRepository.deleteAll()

        user = userRepository.save(
            User(
                username = "emiliano",
                email = "emiliano@example.com"
            )
        )
        anime = titleRepository.save(
            Title(
                title = "Cowboy Bebop",
                mediaType = MediaType.ANIME,
                total = 26
            )
        )
        token = appTokenService.issueFor(user)
    }

    @AfterEach
    fun tearDown() {
        libraryEntryRepository.deleteAll()
    }

    @Test
    fun `authenticated user adds a title to their library`() {
        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "titleId": ${anime.id},
                  "status": "WATCHING"
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/library")
            .then()
            .statusCode(201)
            .body("titleId", equalTo(anime.id.toInt()))
            .body("title", equalTo("Cowboy Bebop"))
            .body("mediaType", equalTo("ANIME"))
            .body("status", equalTo("WATCHING"))
            .body("progress", equalTo(0))
            .body("total", equalTo(26))

        val savedEntry = libraryEntryRepository.findAll().single()
        assertEquals(user.id, savedEntry.user.id)
        assertEquals(anime.id, savedEntry.title.id)
        assertEquals(0, savedEntry.progress)
    }

    @Test
    fun `same user cannot add the same title twice`() {
        val requestBody =
            """
            {
              "titleId": ${anime.id},
              "status": "WATCHING"
            }
            """.trimIndent()

        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .`when`()
            .post("/api/library")
            .then()
            .statusCode(201)

        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(requestBody)
            .`when`()
            .post("/api/library")
            .then()
            .statusCode(409)
    }

    @Test
    fun `anime cannot be added with a manga-only status`() {
        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "titleId": ${anime.id},
                  "status": "READING"
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/library")
            .then()
            .statusCode(400)
    }

    @Test
    fun `unknown title returns not found`() {
        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "titleId": 999999,
                  "status": "WATCHING"
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/library")
            .then()
            .statusCode(404)
    }

    @Test
    fun `library creation requires authentication`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "titleId": ${anime.id},
                  "status": "WATCHING"
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/library")
            .then()
            .statusCode(401)
    }
}
