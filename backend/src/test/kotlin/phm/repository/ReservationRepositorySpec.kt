package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.BOOK_GENRE
import ar.edu.unsam.phm.domain.BOOK_LANGUAGE
import ar.edu.unsam.phm.domain.BOOK_STATE
import ar.edu.unsam.phm.domain.CommonBook
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class ReservationRepositorySpec {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var bookRepository: BookRepository
    @Autowired lateinit var reservationRepository: ReservationRepository
    @Autowired lateinit var ratingRepository: RatingRepository

    lateinit var publisher1: User
    lateinit var publisher2: User
    lateinit var reader1: User
    lateinit var reader2: User
    lateinit var readerWithoutReservations: User

    lateinit var dune: CommonBook
    lateinit var cleanCode: CommonBook
    lateinit var mobyDick: CommonBook

    @BeforeEach
    fun setUp() {
        val today = LocalDate.now()

        reservationRepository.deleteAll()
        ratingRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()

        publisher1 = userRepository.save(createUser("Ada", "Lovelace", "ada@publisher.com", mutableSetOf(USER_TYPE.PUBLISHER)))
        publisher2 = userRepository.save(createUser("Alan", "Turing", "alan@publisher.com", mutableSetOf(USER_TYPE.PUBLISHER)))
        reader1 = userRepository.save(createUser("Grace", "Hopper", "grace@reader.com", mutableSetOf(USER_TYPE.READER)))
        reader2 = userRepository.save(createUser("Katherine", "Johnson", "katherine@reader.com", mutableSetOf(USER_TYPE.READER)))
        readerWithoutReservations = userRepository.save(createUser("Linus", "Torvalds", "linus@reader.com", mutableSetOf(USER_TYPE.READER)))

        dune = bookRepository.save(createBook("Dune", "Frank Herbert", "9789876543211", publisher1)) as CommonBook
        cleanCode = bookRepository.save(createBook("Clean Code", "Robert C. Martin", "9789876543212", publisher1)) as CommonBook
        mobyDick = bookRepository.save(createBook("Moby Dick", "Herman Melville", "9789876543213", publisher2)) as CommonBook

        val reservation1 = Reservation(reader1, today.minusDays(10), today.minusDays(5), dune)
        val reservation2 = Reservation(reader2, today.plusDays(1), today.plusDays(3), dune)
        val reservation3 = Reservation(reader1, today.plusDays(4), today.plusDays(8), cleanCode)
        val reservation4 = Reservation(reader1, today.minusDays(3), today, mobyDick)

        reservationRepository.saveAll(listOf(reservation1, reservation2, reservation3, reservation4))

        dune.addReservation(reservation1)
        dune.addReservation(reservation2)
        cleanCode.addReservation(reservation3)
        mobyDick.addReservation(reservation4)
        bookRepository.saveAll(listOf(dune, cleanCode, mobyDick))
    }

    @Test
    fun `findByPublisherAndFilter devuelve reservas de libros del publisher`() {
        val result = reservationRepository.findByPublisherAndFilter(
            publisher1.id!!,
            "",
            PageRequest.of(0, 2)
        )

        assertEquals(3, result.totalElements)
        assertEquals(2, result.content.size)
        assertTrue(result.content.all { it.getPublisherName() == publisher1.name })
    }

    @Test
    fun `findByPublisherAndFilter filtra por titulo o autor`() {
        val byTitle = reservationRepository.findByPublisherAndFilter(
            publisher1.id!!,
            "dune",
            PageRequest.of(0, 10)
        )
        val byAuthor = reservationRepository.findByPublisherAndFilter(
            publisher1.id!!,
            "martin",
            PageRequest.of(0, 10)
        )

        assertEquals(2, byTitle.totalElements)
        assertEquals(1, byAuthor.totalElements)
    }

    @Test
    fun `findByPublisherAndFilter devuelve vacio si no matchea filtro`() {
        val result = reservationRepository.findByPublisherAndFilter(
            publisher1.id!!,
            "inexistente",
            PageRequest.of(0, 10)
        )

        assertEquals(0, result.totalElements)
    }

    @Test
    fun `findByUserAndFilter devuelve reservas del usuario`() {
        val result = reservationRepository.findByUserAndFilter(
            reader1.id!!,
            "",
            PageRequest.of(0, 2)
        )

        assertEquals(3, result.totalElements)
        assertEquals(2, result.content.size)
    }

    @Test
    fun `findByUserAndFilter filtra por titulo o autor`() {
        val byTitle = reservationRepository.findByUserAndFilter(
            reader1.id!!,
            "clean",
            PageRequest.of(0, 10)
        )
        val byAuthor = reservationRepository.findByUserAndFilter(
            reader1.id!!,
            "melville",
            PageRequest.of(0, 10)
        )

        assertEquals(1, byTitle.totalElements)
        assertEquals(1, byAuthor.totalElements)
    }

    @Test
    fun `findByUserAndFilter devuelve vacio para usuario sin reservas`() {
        val result = reservationRepository.findByUserAndFilter(
            readerWithoutReservations.id!!,
            "",
            PageRequest.of(0, 10)
        )

        assertEquals(0, result.totalElements)
    }

    @Test
    fun `countReservationsByIdPublisher cuenta prestamos de libros de un publisher`() {
        assertEquals(3, reservationRepository.countReservationsByIdPublisher(publisher1.id!!))
        assertEquals(1, reservationRepository.countReservationsByIdPublisher(publisher2.id!!))
    }

    @Test
    fun `countByUserIdAndEndDateBefore cuenta solo reservas finalizadas antes de hoy`() {
        val today = LocalDate.now()

        assertEquals(1, reservationRepository.countByUserIdAndEndDateBefore(reader1.id!!, today))
        assertEquals(0, reservationRepository.countByUserIdAndEndDateBefore(reader2.id!!, today))
    }

    private fun createUser(
        name: String,
        lastname: String,
        email: String,
        roles: MutableSet<USER_TYPE>
    ): User {
        return User(
            name = name,
            lastname = lastname,
            password = "1234",
            email = email,
            phone = "1122334455",
            description = "Usuario de prueba",
            residenceCity = "Buenos Aires",
            type = roles,
            bibliokarma = 0
        )
    }

    private fun createBook(
        title: String,
        author: String,
        isbn: String,
        publisher: User
    ): CommonBook {
        return CommonBook(
            title = title,
            description = "Descripcion de prueba",
            coverUrl = "https://example.com/cover.jpg",
            author = author,
            pages = 300,
            isbn = isbn,
            language = BOOK_LANGUAGE.ENGLISH,
            editorial = "Editorial de prueba",
            publicationDate = LocalDate.of(2000, 1, 1),
            state = BOOK_STATE.EXCELLENT,
            genre = BOOK_GENRE.SCIENCE_FICTION,
        ).apply {
            userPublisher = UserPublisher(publisher.id!!, publisher.name)
        }
    }
}