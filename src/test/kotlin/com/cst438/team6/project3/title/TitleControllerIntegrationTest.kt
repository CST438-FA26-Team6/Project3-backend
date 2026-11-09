package com.cst438.team6.project3.title

import com.cst438.team6.project3.TestcontainersConfiguration
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class TitleControllerIntegrationTest(
    @Autowired context: WebApplicationContext,
    @Autowired private val titleRepository: TitleRepository
) {
    private val mockMvc: MockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(
            springSecurity()
        )
        .build()

    private lateinit var cowboyBebop: Title

    @BeforeEach
    fun setUp() {
        titleRepository.deleteAll()
        cowboyBebop = titleRepository.save(
            Title("Cowboy Bebop", MediaType.ANIME)
        )
        titleRepository.saveAll(
            listOf(
                Title("Cowboy Bebop Shooting Star", MediaType.MANGA),
                Title("Berserk", MediaType.MANGA)
            )
        )
    }

    @Test
    @WithMockUser
    fun `search returns matching titles and media types`() {
        mockMvc.perform(
            get("/api/titles")
                .param("query", "cowboy")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(2)))
            .andExpect(jsonPath("$[0].title").value("Cowboy Bebop"))
            .andExpect(jsonPath("$[0].mediaType").value("ANIME"))
            .andExpect(
                jsonPath("$[1].title")
                    .value("Cowboy Bebop Shooting Star")
            )
            .andExpect(jsonPath("$[1].mediaType").value("MANGA"))
    }

    @Test
    @WithMockUser
    fun `search filters by media type without case sensitivity`() {
        mockMvc.perform(
            get("/api/titles")
                .param("query", "cowboy")
                .param("type", "manga")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(
                jsonPath("$[0].title")
                    .value("Cowboy Bebop Shooting Star")
            )
            .andExpect(jsonPath("$[0].mediaType").value("MANGA"))
    }

    @Test
    @WithMockUser
    fun `title detail returns the requested title`() {
        mockMvc.perform(get("/api/titles/{id}", cowboyBebop.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(cowboyBebop.id))
            .andExpect(jsonPath("$.title").value("Cowboy Bebop"))
            .andExpect(jsonPath("$.mediaType").value("ANIME"))
    }

    @Test
    @WithMockUser
    fun `blank query returns bad request`() {
        mockMvc.perform(
            get("/api/titles")
                .param("query", "   ")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `invalid media type returns bad request`() {
        mockMvc.perform(
            get("/api/titles")
                .param("query", "cowboy")
                .param("type", "book")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `unknown title returns not found`() {
        mockMvc.perform(get("/api/titles/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `title search requires authentication`() {
        mockMvc.perform(
            get("/api/titles")
                .param("query", "cowboy")
        )
            .andExpect(status().isUnauthorized)
    }
}
