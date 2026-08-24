package ar.edu.unsam.phm.integration

import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.user.LoginRequest
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.RatingRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
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
class UserIntegrationTest {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var reservationRepository: ReservationRepository
    @Autowired lateinit var bookRepository: BookRepository
    @Autowired lateinit var ratingRepository: RatingRepository
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var mockMvc: MockMvc

    private val passwordEncoder: PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()!!

    lateinit var existingUser: User
    lateinit var bearer: String

    @BeforeEach
    fun setUp() {
        ratingRepository.deleteAll()
        reservationRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()

        existingUser = userRepository.save(
            User(
                "fran", "demaino", passwordEncoder.encode("1234"), "frandemaino@gmail.com",
                "4789075423", "Bio", "Buenos Aires", mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER)
            )
        )

        bearer = loginAndGetBearer("frandemaino@gmail.com", "1234")
    }

    @Test
    fun `createAccount devuelve 204 y persiste usuario normalizado`() {
        val req = mapOf(
            "fullName" to "  VALEN   ROSSI ",
            "email" to "valentina.rossi@gmail.com",
            "password" to "abc123"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isNoContent)

        val created = userRepository.findByEmail("valentina.rossi@gmail.com").orElseThrow()
        assertEquals("valen", created.name)
        assertEquals("rossi", created.lastname)
    }

    @Test
    fun `createAccount con email existente devuelve 400`() {
        val req = mapOf(
            "fullName" to "Otro Usuario",
            "email" to "frandemaino@gmail.com",
            "password" to "abc123"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getUserById devuelve 200 con datos del usuario`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/user/${existingUser.id}")
                .header("Authorization", bearer)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(existingUser.id!!))
            .andExpect(jsonPath("$.name").value("fran"))
    }

    @Test
    fun `updateUserProfile devuelve 204 y actualiza el usuario`() {
        val req = mapOf(
            "name" to "Francisco",
            "lastname" to "Demaino",
            "email" to "fran.updated@gmail.com",
            "phone" to "1111222233",
            "description" to "Una descripcion valida",
            "residenceCity" to "CABA",
            "roles" to listOf("Lector")
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/user/${existingUser.id}")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isNoContent)

        val updated = userRepository.findById(existingUser.id!!).orElseThrow()
        assertEquals("Francisco", updated.name)
        assertEquals("fran.updated@gmail.com", updated.email)
        assertEquals("1111222233", updated.phone)
    }

    @Test
    fun `getHeaderData devuelve 200 con datos de cabecera`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/user/header/${existingUser.id}")
                .header("Authorization", bearer)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("fran"))
            .andExpect(jsonPath("$.lastname").value("demaino"))
            .andExpect(jsonPath("$.bibliokarma").value(existingUser.bibliokarma))
    }

    private fun loginAndGetBearer(email: String, password: String): String {
        val loginBody = LoginRequest(email, password)

        val loginResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginBody))
        )
            .andExpect(status().isOk)
            .andReturn()

        val responseBody = loginResponse.response.contentAsString
        val token = objectMapper.readTree(responseBody).get("token").asText()

        return "Bearer $token"
    }
}
