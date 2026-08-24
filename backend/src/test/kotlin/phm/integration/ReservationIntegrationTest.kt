package ar.edu.unsam.phm.integration

import ar.edu.unsam.phm.domain.BOOK_GENRE
import ar.edu.unsam.phm.domain.BOOK_LANGUAGE
import ar.edu.unsam.phm.domain.BOOK_STATE
import ar.edu.unsam.phm.domain.CommonBook
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserPublisher
import ar.edu.unsam.phm.dto.user.LoginRequest
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.RatingRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
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
class ReservationIntegrationTest {

    @Autowired
    lateinit var reservationRepository: ReservationRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var bookRepository: BookRepository

    @Autowired
    lateinit var ratingRepository: RatingRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var mockMvc: MockMvc

    lateinit var testUser: User
    lateinit var otherUser: User
    lateinit var bearer: String
    lateinit var today: LocalDate

    private val passwordEncoder: PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()!!


    @BeforeEach
    fun setup() {
        today = LocalDate.now()

        reservationRepository.deleteAll()
        ratingRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()

        testUser = userRepository.save(createUser("frandemaino", "1234"))
        otherUser = userRepository.save(createUser("pepe", "pepe"))

        bearer = loginAndGetBearer("frandemaino@gmail.com", "1234")
    }

    @Test
    fun `obtener todas las reservas que le pertenecen a un usuario`() {
        val book = bookRepository.save(createBook(otherUser)) as CommonBook

        val r1 = Reservation(testUser, today.plusMonths(1), today.plusMonths(2), book)
        val r2 = Reservation(testUser, today.plusWeeks(1), today.plusWeeks(2), book)
        val r3 = Reservation(otherUser, today.plusDays(1), today.plusDays(2), book)

        reservationRepository.saveAll(listOf(r1, r2, r3))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/reservation/myReservations")
                .header("Authorization", bearer)
                .param("idUser", testUser.id.toString())
                .param("page", "1")
                .param("size", "10")
                .param("filter", "")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalReservations").value(2))
            .andExpect(jsonPath("$.reservations.length()").value(2))
    }

    @Test
    fun `obtener todas las reservas en las que el usuario es el propietario del libro`() {
        val foreignBook = bookRepository.save(createBook(otherUser)) as CommonBook
        val ownedBook = bookRepository.save(createBook(testUser)) as CommonBook

        val r1 = Reservation(testUser, today.plusMonths(1), today.plusMonths(2), foreignBook)
        val r2 = Reservation(testUser, today.plusWeeks(1), today.plusWeeks(2), foreignBook)
        val r3 = Reservation(otherUser, today.plusDays(1), today.plusDays(2), ownedBook)

        reservationRepository.saveAll(listOf(r1, r2, r3))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/reservation/ownedBooksReservations")
                .header("Authorization", bearer)
                .param("idUser", testUser.id.toString())
                .param("page", "1")
                .param("size", "10")
                .param("filter", "")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalReservations").value(1))
            .andExpect(jsonPath("$.reservations.length()").value(1))
    }

    @Test
    fun `Se crea y almacena una reserva correctamente` () {
        val book = bookRepository.save(createBook(otherUser)) as CommonBook

        val request = mapOf(
            "idBook" to book.id,
            "startDate" to today.plusDays(5).toString(),
            "endDate" to today.plusDays(10).toString()
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/reservation/create")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        val reservations = reservationRepository.findAll()
        assert(reservations.size == 1)
    }

    @Test
    fun `no permite crear una reserva en fechas ocupadas`() {
        val book = bookRepository.save(createBook(otherUser)) as CommonBook

        val existing = Reservation(
            testUser,
            today.plusDays(5),
            today.plusDays(10),
            book
        )
        reservationRepository.save(existing)
        book.addReservation(existing)
        bookRepository.save(book)

        val request = mapOf(
            "idUser" to testUser.id,
            "idBook" to book.id,
            "startDate" to today.plusDays(7).toString(),
            "endDate" to today.plusDays(12).toString()
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/reservation/create")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `no permite crear reserva con libro inexistente`() {
        val request = mapOf(
            "idBook" to "book-inexistente",
            "startDate" to today.plusDays(5).toString(),
            "endDate" to today.plusDays(10).toString()
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/reservation/create")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `obtiene bibliokarma correctamente`() {
        val book = bookRepository.save(createBook(otherUser)) as CommonBook

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/reservation/bibliokarma")
                .header("Authorization", bearer)
                .param("idUser", testUser.id.toString())
                .param("idBook", book.id)
                .param("reservationStartDate", today.plusDays(1).toString())
                .param("reservationEndDate", today.plusDays(5).toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.bibliokarma").exists())
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

    private fun createUser(name: String, password: String) = User(
        name,
        "Crawford",
        passwordEncoder.encode(password),
        "$name@gmail.com",
        "4789075423",
        "Breve descripcion de mi persona",
        "Buenos Aires",
        mutableSetOf(USER_TYPE.PUBLISHER, USER_TYPE.READER)
    )

    private fun createBook(p: User) = CommonBook(
        "book",
        "a normal book",
        "https://example.com/cover.png",
        "author",
        100,
        "9791234512345",
        BOOK_LANGUAGE.ENGLISH,
        "edit",
        LocalDate.now().minusDays(1),
        BOOK_STATE.GOOD,
        BOOK_GENRE.DRAMA
    ).apply {
        userPublisher = UserPublisher(p.id!!, p.name)
    }
}