package ar.edu.unsam.phm.integration

import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.user.LoginRequest
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.RatingRepository
import ar.edu.unsam.phm.repository.RefreshTokenRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired lateinit var reservationRepository: ReservationRepository
    @Autowired lateinit var bookRepository: BookRepository
    @Autowired lateinit var ratingRepository: RatingRepository
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var mockMvc: MockMvc

    private val passwordEncoder: PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()!!

    @BeforeEach
    fun setUp() {
        refreshTokenRepository.deleteAll()
        ratingRepository.deleteAll()
        reservationRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()

        userRepository.save(
            User(
                "frandemaino", "Crawford", passwordEncoder.encode("1234"), "frandemaino@gmail.com",
                "4789075423", "Breve descripcion de mi persona", "Buenos Aires",
                mutableSetOf(USER_TYPE.PUBLISHER, USER_TYPE.READER)
            )
        )
    }

    @Test
    fun `login devuelve 200 con token y refreshToken`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("frandemaino@gmail.com", "1234")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty)

        assertEquals(1, refreshTokenRepository.count())
    }

    @Test
    fun `login con password invalida devuelve 401`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("frandemaino@gmail.com", "incorrecta")))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `refresh devuelve 200 y revoca el refresh token anterior`() {
        val loginResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest("frandemaino@gmail.com", "1234")))
        )
            .andExpect(status().isOk)
            .andReturn()

        val refreshToken = objectMapper.readTree(loginResponse.response.contentAsString).get("refreshToken").asText()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("refreshToken" to refreshToken)))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty)

        val tokens = refreshTokenRepository.findAll()
        assertEquals(2, tokens.size)
        assertTrue(tokens.count { it.revoked } == 1)
    }

    @Test
    fun `refresh con token inexistente devuelve 401`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("refreshToken" to "inexistente")))
        )
            .andExpect(status().isUnauthorized)
    }
}
