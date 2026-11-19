package com.cst438.team6.project3.user

import com.cst438.team6.project3.TestcontainersConfiguration
import com.cst438.team6.project3.library.LibraryEntry
import com.cst438.team6.project3.library.LibraryEntryRepository
import com.cst438.team6.project3.library.LibraryStatus
import com.cst438.team6.project3.service.AppTokenService
import com.cst438.team6.project3.title.MediaType
import com.cst438.team6.project3.title.Title
import com.cst438.team6.project3.title.TitleRepository
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(TestcontainersConfiguration::class)
class UserApiIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var identityRepository: IdentityRepository

    @Autowired
    private lateinit var libraryEntryRepository: LibraryEntryRepository

    @Autowired
    private lateinit var titleRepository: TitleRepository

    @Autowired
    private lateinit var appTokenService: AppTokenService

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port

        libraryEntryRepository.deleteAll()
        identityRepository.deleteAll()
        userRepository.deleteAll()
        titleRepository.deleteAll()
    }

    @Test
    fun `delete account removes user identities and library data`() {
        val user = userRepository.save(
            User(
                username = "user",
                email = "user@example.com"
            )
        )

        val identity = identityRepository.save(
            Identity(
                user = user,
                provider = AuthProvider.GITHUB,
                providerUserId = "github-user"
            )
        )

        val title = titleRepository.save(
            Title(
                title = "Test Title",
                mediaType = MediaType.ANIME
            )
        )

        libraryEntryRepository.save(
            LibraryEntry(
                user = user,
                title = title,
                status = LibraryStatus.WATCHING
            )
        )

        val token = appTokenService.issueFor(user)

        given()
            .auth()
            .oauth2(token)
            .`when`()
            .delete("/api/users/me")
            .then()
            .statusCode(204)

        assertFalse(
            userRepository.existsById(user.id!!)
        )

        assertFalse(
            identityRepository.existsById(identity.id!!)
        )

        assertFalse(
            libraryEntryRepository.findAll()
                .any { it.user.id == user.id }
        )

        assertTrue(
            titleRepository.existsById(title.id)
        )
    }
}