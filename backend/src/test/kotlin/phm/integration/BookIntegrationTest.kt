package ar.edu.unsam.phm.integration

import ar.edu.unsam.phm.domain.BOOK_GENRE
import ar.edu.unsam.phm.domain.BOOK_LANGUAGE
import ar.edu.unsam.phm.domain.BOOK_STATE
import ar.edu.unsam.phm.domain.CommonBook
import ar.edu.unsam.phm.domain.Rating
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserPublisher
import ar.edu.unsam.phm.dto.RatingResponseDTO
import ar.edu.unsam.phm.dto.user.LoginRequest
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.RatingRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookIntegrationTest {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var bookRepository: BookRepository
    @Autowired lateinit var ratingRepository: RatingRepository
    @Autowired lateinit var reservationRepository: ReservationRepository
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    private val passwordEncoder: PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()!!

    lateinit var user1: User
    lateinit var user2: User
    lateinit var user3: User
    lateinit var book1: CommonBook
    lateinit var book2: CommonBook
    lateinit var book3: CommonBook
    lateinit var book4: CommonBook
    lateinit var book5: CommonBook

    @BeforeEach
    fun setUp() {
        ratingRepository.deleteAll()
        reservationRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()
        initEntities()
    }

    @Test
    fun `DELETE Book con login real devuelve 204 y cambia isActive a false`() {
        val bearer = loginAndGetBearer("frandemaino@gmail.com", "1234")

        assertTrue(bookRepository.findById(book1.id).orElseThrow().isActive)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/books/${book1.id}")
                .header("Authorization", bearer)
        ).andExpect(status().isNoContent)

        assertFalse(bookRepository.findById(book1.id).orElseThrow().isActive)
    }

    @Test
    fun `RESTORE Book devuelve 204 y cambia isActive a true`() {
        val bearer = loginAndGetBearer("frandemaino@gmail.com", "1234")

        book1.isActive = false
        bookRepository.save(book1)

        assertFalse(bookRepository.findById(book1.id).orElseThrow().isActive)

        mockMvc.perform(
            MockMvcRequestBuilders.put("/books/${book1.id}/restore")
                .header("Authorization", bearer)
        ).andExpect(status().isNoContent)

        assertTrue(bookRepository.findById(book1.id).orElseThrow().isActive)
    }

    @Test
    fun `fillForm devuelve 200 y trae campos editables del libro`() {
        val bearer = loginAndGetBearer("frandemaino@gmail.com", "1234")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/books/form/${book1.id}")
                .header("Authorization", bearer)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Dune"))

        assertTrue(bookRepository.findById(book1.id).orElseThrow().isActive)
    }

    @Test
    fun `Un publisher puede crear un libro`() {
        val bearer = loginAndGetBearer("frandemaino@gmail.com", "1234")

        val newBook = mapOf(
            "title" to "asd",
            "author" to "authorRandom",
            "editorial" to "hemingway",
            "isbn" to "9780000022222",
            "pages" to 200,
            "bookType" to "Con dedicatoria",
            "genre" to "Drama",
            "language" to "Francés",
            "state" to "Bueno",
            "publicationDate" to "2000-10-01",
            "description" to "asdasdasd",
            "coverUrl" to "https://png.pngtree.com/png-vector/20250513/ourmid/pngtree-colorful-books-pens-and-ruler-back-to-school-stationery-png-image_16265965.png"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/books/create")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook))
        ).andExpect(status().isCreated)

        assertEquals(6, bookRepository.findAll().count())
    }

    @Test
    fun `getFilteredBooks devuelve 200 con libros filtrados`() {
        val bearer = loginAndGetBearer("frandemaino@gmail.com", "1234")

        val filters = mapOf("order" to "TITLE")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/books/filtered")
                .param("page", "1")
                .param("size", "3")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filters))
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalFilteredBooks").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$.books.length()").value(3))
    }

    @Test
    fun `getMyBooks devuelve 200 con libros del publisher`() {
        val bearer = loginAndGetBearer("frandemaino@gmail.com", "1234")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/books/mybooks")
                .param("userId", user1.id.toString())
                .param("page", "1")
                .param("filterBy", "ALL")
                .param("orderBy", "TITLE")
                .param("isASC", "true")
                .header("Authorization", bearer)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalSize").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.books.length()").value(3))
    }

    @Test
    fun `getBookById devuelve 200 con detalle del libro`() {
        val bearer = loginAndGetBearer("frandemaino@gmail.com", "1234")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/books/book-detail/${book5.id}")
                .param("idUser", user1.id.toString())
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(1).toString())
                .header("Authorization", bearer)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Viaje al fin de la noche"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.ratingCount").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstTwoRatings.length()").value(1))
    }

    @Test
    fun `updateBook devuelve 204 y actualiza campos del libro`() {
        val bearer = loginAndGetBearer("frandemaino@gmail.com", "1234")

        val updateBody = mapOf(
            "title" to "Dune Messiah",
            "description" to "Nueva descripcion",
            "coverUrl" to "https://m.media-amazon.com/images/I/71sF8kuMW3L._SY466_.jpg",
            "author" to "Frank Herbert",
            "pages" to 250,
            "isbn" to "9789876543211",
            "editorial" to "Ace",
            "publicationDate" to "1980-01-01",
            "language" to "Inglés",
            "state" to "Muy bueno",
            "genre" to "Ciencia ficción"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/books/update/${book1.id}")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBody))
        ).andExpect(status().isNoContent)

        val updated = bookRepository.findById(book1.id).orElseThrow()
        assertEquals("Dune Messiah", updated.title)
        assertEquals("Frank Herbert", updated.author)
        assertEquals(250, updated.pages)
        assertEquals(BOOK_STATE.VERY_GOOD, updated.state)
        assertEquals(BOOK_GENRE.SCIENCE_FICTION, updated.genre)
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

    private fun initEntities() {
        user1 = userRepository.save(
            User(
                "User1", "Demaino", passwordEncoder.encode("1234"), "frandemaino@gmail.com",
                "4789075423", "Me gusta leer libros de ciencia ficción y fantasía.",
                "Buenos Aires", mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER), 0
            )
        )

        user2 = userRepository.save(
            User(
                "Z_publisher", "", passwordEncoder.encode("1234"), "admin@gmail.com",
                "10000000", "Soy el dios de la aplicación!!!",
                "adminlandia", mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER), 1000
            )
        )

        user3 = userRepository.save(
            User(
                "Z_user", "", passwordEncoder.encode("1234"), "valentina.rossi@gmail.com",
                "10000000", "Soy el dios de la aplicación!!!",
                "adminlandia", mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER), 1000
            )
        )

        fun common(title: String, publisher: User): CommonBook = CommonBook(
            title = title,
            description = "sum random art bs",
            coverUrl = "https://m.media-amazon.com/images/I/71sF8kuMW3L._SY466_.jpg",
            author = "Donald A. Norman",
            pages = 368,
            isbn = "9789876543211",
            language = BOOK_LANGUAGE.ENGLISH,
            editorial = "Basic Books",
            publicationDate = LocalDate.of(1988, 8, 1),
            state = BOOK_STATE.EXCELLENT,
            genre = BOOK_GENRE.DESIGN,
        ).apply {
            userPublisher = UserPublisher(publisher.id!!, publisher.name)
        }

        book1 = common("Dune", user1)
        book2 = common("B_active", user2)
        book3 = common("Doctor House", user1)
        book4 = common("B_inactive", user2)
        book5 = common("Viaje al fin de la noche", user1)

        book1 = bookRepository.save(book1) as CommonBook
        book2 = bookRepository.save(book2) as CommonBook
        book3 = bookRepository.save(book3) as CommonBook
        book4 = bookRepository.save(book4) as CommonBook
        book5 = bookRepository.save(book5) as CommonBook

        val reservation1 = Reservation(
            user = user3,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusWeeks(2),
            book = book5
        )
        reservationRepository.save(reservation1)
        book5.addReservation(reservation1)

        val rating2 = Rating(
            user = user2,
            calification = 4.5,
            comment = "Estuvo bueno",
            bookId = book5.id
        )
        ratingRepository.save(rating2)
        book5.ratingCount = 1
        book5.calification = 4.5
        book5.firstTwoRatings = mutableListOf(RatingResponseDTO.createFrom(rating2))

        bookRepository.save(book5)
    }

}