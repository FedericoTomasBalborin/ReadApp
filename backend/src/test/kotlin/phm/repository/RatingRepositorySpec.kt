package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.BOOK_GENRE
import ar.edu.unsam.phm.domain.BOOK_LANGUAGE
import ar.edu.unsam.phm.domain.BOOK_STATE
import ar.edu.unsam.phm.domain.CommonBook
import ar.edu.unsam.phm.domain.DedicationBook
import ar.edu.unsam.phm.domain.Rating
import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Limit
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class RatingRepositorySpec {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var bookRepository: BookRepository
    @Autowired lateinit var ratingRepository: RatingRepository
    @Autowired lateinit var reservationRepository: ReservationRepository

    lateinit var user1: User
    lateinit var user5: User
    lateinit var commonBook: CommonBook
    lateinit var dedicationBook: DedicationBook

    @BeforeEach
    fun setUp() {
        reservationRepository.deleteAll()
        ratingRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()

        user1 = userRepository.save(
            User(
                "Fran", "Demaino", "1234", "frandemaino@gmail.com",
                "4789075423", "Me gusta leer libros de ciencia ficción y fantasía.",
                "Buenos Aires", mutableSetOf(USER_TYPE.READER), 0
            )
        )
        user5 = userRepository.save(
            User(
                "admin", "", "1234", "admin@gmail.com",
                "10000000", "Soy el dios de la aplicación!!!",
                "adminlandia", mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER), 1000
            )
        )

        commonBook = bookRepository.save(
            CommonBook(
                title = "Dune",
                description = "The messiah rises on Arrakis.",
                coverUrl = "https://upload.wikimedia.org/wikipedia/commons/7/7c/Cima_da_Conegliano%2C_God_the_Father.jpg",
                author = "Frank Herbert",
                pages = 412,
                isbn = "9799876543211",
                language = BOOK_LANGUAGE.ENGLISH,
                editorial = "Ace",
                publicationDate = LocalDate.of(1965, 8, 1),
                state = BOOK_STATE.EXCELLENT,
                genre = BOOK_GENRE.SCIENCE_FICTION,
            ).apply { userPublisher = UserPublisher(user5.id!!, user5.name) }
        ) as CommonBook

        dedicationBook = bookRepository.save(
            DedicationBook(
                title = "The design of everyday things",
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
            ).apply { userPublisher = UserPublisher(user1.id!!, user1.name) }
        ) as DedicationBook

        ratingRepository.save(
            Rating(
                user = user1,
                calification = 5.0,
                comment = "Excelente libro",
                bookId = commonBook.id
            )
        )
    }

    @Test
    fun `findByBookId devuelve los ratings del libro`() {
        val result = ratingRepository.findByBookId(commonBook.id, Limit.unlimited())

        assertEquals(1, result.size)
        assertEquals("Fran", result[0].user.name)
        assertEquals(5.0, result[0].calification)
        assertEquals("Excelente libro", result[0].comment)
    }

    @Test
    fun `findByBookId devuelve vacio si el libro no tiene ratings`() {
        val result = ratingRepository.findByBookId(dedicationBook.id, Limit.unlimited())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRatingAverageByBookId devuelve promedio y cantidad`() {
        val stats = ratingRepository.getRatingAverageByBookId(commonBook.id)

        assertEquals(5.0, stats.getAverage())
        assertEquals(1, stats.getCount())
    }
}