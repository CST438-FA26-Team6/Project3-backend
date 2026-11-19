package com.cst438.team6.project3.title

import com.cst438.team6.project3.TestcontainersConfiguration
import com.cst438.team6.project3.library.LibraryEntryRepository
import com.cst438.team6.project3.service.AppTokenService
import com.cst438.team6.project3.user.User
import com.cst438.team6.project3.user.UserRepository
import com.cst438.team6.project3.user.UserRole
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(TestcontainersConfiguration::class)
class AdminTitleControllerIntegrationTest {
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

    private lateinit var anime: Title
    private lateinit var adminToken: String
    private lateinit var userToken: String

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port

        libraryEntryRepository.deleteAll()
        userRepository.deleteAll()
        titleRepository.deleteAll()

        val admin = userRepository.save(
            User(
                username = "admin",
                email = "admin@example.com",
                role = UserRole.ADMIN
            )
        )
        val user = userRepository.save(
            User(
                username = "viewer",
                email = "viewer@example.com"
            )
        )
        anime = titleRepository.save(
            Title(
                title = "Cowboy Bebop",
                mediaType = MediaType.ANIME,
                total = 26
            )
        )
        adminToken = appTokenService.issueFor(admin)
        userToken = appTokenService.issueFor(user)
    }

    @Test
    fun `admin updates a title`() {
        given()
            .auth()
            .oauth2(adminToken)
            .contentType(ContentType.JSON)
            .body(updateRequestBody())
            .`when`()
            .put("/api/titles/{id}", anime.id)
            .then()
            .statusCode(200)
            .body("id", equalTo(anime.id.toInt()))
            .body("title", equalTo("Cowboy Bebop Remastered"))
            .body("mediaType", equalTo("MANGA"))
            .body("total", equalTo(12))

        val updated = titleRepository.findById(anime.id).orElseThrow()
        assertEquals("Cowboy Bebop Remastered", updated.title)
        assertEquals(MediaType.MANGA, updated.mediaType)
        assertEquals(12, updated.total)
    }

    @Test
    fun `admin deletes a title`() {
        assertTrue(titleRepository.existsById(anime.id))

        given()
            .auth()
            .oauth2(adminToken)
            .`when`()
            .delete("/api/titles/{id}", anime.id)
            .then()
            .statusCode(204)

        assertFalse(titleRepository.existsById(anime.id))
    }

    @Test
    fun `regular user cannot update a title`() {
        given()
            .auth()
            .oauth2(userToken)
            .contentType(ContentType.JSON)
            .body(updateRequestBody())
            .`when`()
            .put("/api/titles/{id}", anime.id)
            .then()
            .statusCode(403)

        assertEquals(
            "Cowboy Bebop",
            titleRepository.findById(anime.id).orElseThrow().title
        )
    }

    @Test
    fun `regular user cannot delete a title`() {
        given()
            .auth()
            .oauth2(userToken)
            .`when`()
            .delete("/api/titles/{id}", anime.id)
            .then()
            .statusCode(403)

        assertTrue(titleRepository.existsById(anime.id))
    }

    @Test
    fun `title management requires authentication`() {
        given()
            .contentType(ContentType.JSON)
            .body(updateRequestBody())
            .`when`()
            .put("/api/titles/{id}", anime.id)
            .then()
            .statusCode(401)

        given()
            .`when`()
            .delete("/api/titles/{id}", anime.id)
            .then()
            .statusCode(401)
    }

    @Test
    fun `admin receives not found for an unknown title`() {
        given()
            .auth()
            .oauth2(adminToken)
            .contentType(ContentType.JSON)
            .body(updateRequestBody())
            .`when`()
            .put("/api/titles/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(404)

        given()
            .auth()
            .oauth2(adminToken)
            .`when`()
            .delete("/api/titles/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(404)
    }

    private fun updateRequestBody(): String {
        return """
            {
              "title": "Cowboy Bebop Remastered",
              "mediaType": "MANGA",
              "total": 12
            }
        """.trimIndent()
    }
}
