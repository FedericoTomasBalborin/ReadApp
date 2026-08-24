package ar.edu.unsam.phm.integration

import ar.edu.unsam.phm.domain.BOOK_GENRE
import ar.edu.unsam.phm.domain.BOOK_LANGUAGE
import ar.edu.unsam.phm.domain.BOOK_STATE
import ar.edu.unsam.phm.domain.CommonBook
import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserPublisher
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
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RatingIntegrationTest {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var bookRepository: BookRepository
    @Autowired lateinit var ratingRepository: RatingRepository
    @Autowired lateinit var reservationRepository: ReservationRepository
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var mockMvc: MockMvc

    private val passwordEncoder: PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()!!

    lateinit var publisher: User
    lateinit var reader: User
    lateinit var book: CommonBook
    lateinit var bearerReader: String

    @BeforeEach
    fun setUp() {
        ratingRepository.deleteAll()
        reservationRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()

        publisher = userRepository.save(
            User(
                "publisher", "One", passwordEncoder.encode("1234"), "publisher@gmail.com",
                "4789075423", "publisher", "Buenos Aires", mutableSetOf(USER_TYPE.PUBLISHER)
            )
        )

        reader = userRepository.save(
            User(
                "reader", "One", passwordEncoder.encode("1234"), "reader@gmail.com",
                "4789075423", "reader", "Buenos Aires", mutableSetOf(USER_TYPE.READER)
            )
        )

        book = bookRepository.save(
            CommonBook(
                "Dune",
                "a normal book",
                "https://example.com/cover.png",
                "Frank Herbert",
                300,
                "9781234567890",
                BOOK_LANGUAGE.ENGLISH,
                "Ace",
                LocalDate.of(1980, 1, 1),
                BOOK_STATE.GOOD,
                BOOK_GENRE.SCIENCE_FICTION
            ).apply { userPublisher = UserPublisher(this@RatingIntegrationTest.publisher.id!!, this@RatingIntegrationTest.publisher.name) }
        ) as CommonBook

        bearerReader = loginAndGetBearer("reader@gmail.com", "1234")
    }

    @Test
    fun `createRating devuelve 201 y persiste el rating`() {
        val req = mapOf(
            "idBook" to book.id,
            "calification" to 4.5,
            "comment" to "Muy buen libro"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/rating/create")
                .header("Authorization", bearerReader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)

        val persistedBook = bookRepository.findById(book.id).orElseThrow()
        assertEquals(1, persistedBook.ratingCount)
    }

    @Test
    fun `createRating duplicado devuelve 400`() {
        val req = mapOf(
            "idBook" to book.id,
            "calification" to 4.0,
            "comment" to "Primera reseña"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/rating/create")
                .header("Authorization", bearerReader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/rating/create")
                .header("Authorization", bearerReader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `createRating con libro inexistente devuelve 404`() {
        val req = mapOf(
            "idBook" to "libro-inexistente",
            "calification" to 4.0,
            "comment" to "No deberia crearse"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/rating/create")
                .header("Authorization", bearerReader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getRatingByBook devuelve 200 con lista de ratings`() {
        val req = mapOf(
            "idBook" to book.id,
            "calification" to 5.0,
            "comment" to "Excelente"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/rating/create")
                .header("Authorization", bearerReader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/rating/getRatingByBook/${book.id}")
                .header("Authorization", bearerReader)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].username").value("reader"))
            .andExpect(jsonPath("$[0].calification").value(5.0))
            .andExpect(jsonPath("$[0].comment").value("Excelente"))
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
