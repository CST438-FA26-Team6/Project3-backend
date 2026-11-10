package com.cst438.team6.project3.auth

import com.cst438.team6.project3.TestcontainersConfiguration
import com.cst438.team6.project3.service.AppTokenService
import com.cst438.team6.project3.service.GitHubIdentityClient
import com.cst438.team6.project3.title.MediaType
import com.cst438.team6.project3.title.Title
import com.cst438.team6.project3.title.TitleRepository
import com.cst438.team6.project3.user.AuthProvider
import com.cst438.team6.project3.user.IdentityRepository
import com.cst438.team6.project3.user.User
import com.cst438.team6.project3.user.UserRepository
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(TestcontainersConfiguration::class)
class AuthApiIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var identityRepository: IdentityRepository

    @Autowired
    private lateinit var titleRepository: TitleRepository

    @Autowired
    private lateinit var appTokenService: AppTokenService

    @MockitoBean
    private lateinit var gitHubIdentityClient: GitHubIdentityClient

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port

        identityRepository.deleteAll()
        userRepository.deleteAll()
        titleRepository.deleteAll()
    }

    @Test
    fun `GitHub authentication creates account and returns application JWT`() {
        val identity = ProviderIdentity(
            provider = AuthProvider.GITHUB,
            providerUserId = "github-123",
            username = "jessika",
            email = "jessika@example.com",
            emailVerified = true
        )

        `when`(
            gitHubIdentityClient.exchange(
                "github-code",
                "github-verifier",
                "http://localhost:8081/auth"
            )
        ).thenReturn(identity)

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "code": "github-code",
                  "codeVerifier": "github-verifier",
                  "redirectUri": "http://localhost:8081/auth"
                }
                """.trimIndent()
            )
            .`when`()
            .post("/auth/github")
            .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("user.username", equalTo("jessika"))
            .body(
                "user.email",
                equalTo("jessika@example.com")
            )
            .body("user.role", equalTo("USER"))

        val savedUser =
            userRepository.findByEmailIgnoreCase(
                "jessika@example.com"
            )

        assertNotNull(savedUser)

        val savedIdentity =
            identityRepository.findByProviderAndProviderUserId(
                AuthProvider.GITHUB,
                "github-123"
            )

        assertNotNull(savedIdentity)

        assertEquals(
            savedUser.id,
            savedIdentity.user.id
        )
    }

    @Test
    fun `protected endpoint without application JWT returns 401`() {
        given()
            .queryParam("query", "Cowboy")
            .`when`()
            .get("/api/titles")
            .then()
            .statusCode(401)
    }

    @Test
    fun `protected endpoint accepts valid application JWT`() {
        val user = userRepository.save(
            User(
                username = "jessika",
                email = "jessika@example.com"
            )
        )

        titleRepository.save(
            Title(
                title = "Cowboy Bebop",
                mediaType = MediaType.ANIME
            )
        )

        val token = appTokenService.issueFor(user)

        given()
            .auth()
            .oauth2(token)
            .queryParam("query", "Cowboy")
            .`when`()
            .get("/api/titles")
            .then()
            .statusCode(200)
            .body(
                "[0].title",
                equalTo("Cowboy Bebop")
            )
            .body(
                "[0].mediaType",
                equalTo("ANIME")
            )
    }
}