package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.BOOK_GENRE
import ar.edu.unsam.phm.domain.BOOK_LANGUAGE
import ar.edu.unsam.phm.domain.BOOK_STATE
import ar.edu.unsam.phm.domain.CommonBook
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserPublisher
import ar.edu.unsam.phm.dto.BOOKFILTER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class BookRepositoryJPASpec {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var bookRepository: BookRepository
    @Autowired lateinit var reservationRepository: ReservationRepository

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
        reservationRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()
        initEntities()
    }

    @Test
    fun `findFilteredBooks retorna libros activos`() {
        val page = bookRepository.findFilteredBooks(
            title = null,
            isbn = null,
            genres = null,
            maxPages = null,
            minPages = null,
            username = null,
            fromDate = null,
            toDate = null,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(5, page.totalElements)
        assertTrue(page.content.any { it.id == book5.id && it.title == "Viaje al fin de la noche" })
    }

    @Test
    fun `findFilteredBooks por rango de fechas excluye libros reservados en ese periodo`() {
        val page = bookRepository.findFilteredBooks(
            title = null,
            isbn = null,
            genres = null,
            maxPages = null,
            minPages = null,
            username = null,
            fromDate = LocalDate.now(),
            toDate = LocalDate.now().plusDays(1),
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(4, page.totalElements)
        assertFalse(page.content.any { it.id == book5.id })
    }

    @Test
    fun `findMyBooks ALL trae los libros del publisher con disponibilidad calculable`() {
        val page = bookRepository.findMyBooks(
            publisherId = user1.id!!,
            filter = BOOKFILTER.ALL.value,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(3, page.totalElements)

        val b1 = page.content.first { it.id == book1.id }
        val b3 = page.content.first { it.id == book3.id }
        val b5 = page.content.first { it.id == book5.id }

        assertTrue(b1.isAvailable(LocalDate.now(), LocalDate.now().plusDays(1)))
        assertTrue(b3.isAvailable(LocalDate.now(), LocalDate.now().plusDays(1)))
        assertFalse(b5.isAvailable(LocalDate.now(), LocalDate.now().plusDays(1)))
    }

    @Test
    fun `findMyBooks AVAILABLE trae solo libros activos y disponibles`() {
        val page = bookRepository.findMyBooks(
            publisherId = user1.id!!,
            filter = BOOKFILTER.AVAILABLE.value,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(2, page.totalElements)
        assertTrue(page.content.all { it.isAvailable(LocalDate.now(), LocalDate.now().plusDays(1)) })
    }

    @Test
    fun `findMyBooks BORROWED trae solo libros activos con solapamiento de reserva`() {
        val page = bookRepository.findMyBooks(
            publisherId = user1.id!!,
            filter = BOOKFILTER.BORROWED.value,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(1, page.totalElements)
        assertEquals("Viaje al fin de la noche", page.content.first().title)
    }

    @Test
    fun `findMyBooks DELETED retorna solo inactivos`() {
        book3.isActive = false
        bookRepository.save(book3)

        val page = bookRepository.findMyBooks(
            publisherId = user1.id!!,
            filter = BOOKFILTER.DELETED.value,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(1, page.totalElements)
        assertEquals(book3.id, page.content.first().id)
    }

    @Test
    fun `findMyBooks ordena por author asc y desc`() {
        book1.author = "Asimov"
        book3.author = "Borges"
        book5.author = "Cortazar"
        bookRepository.saveAll(listOf(book1, book3, book5))

        val pageAsc = bookRepository.findMyBooks(
            publisherId = user1.id!!,
            filter = BOOKFILTER.ALL.value,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            pageable = PageRequest.of(0, 10, Sort.by("author").ascending())
        )

        assertEquals(listOf("Asimov", "Borges", "Cortazar"), pageAsc.content.map { it.author })

        val pageDesc = bookRepository.findMyBooks(
            publisherId = user1.id!!,
            filter = BOOKFILTER.ALL.value,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            pageable = PageRequest.of(0, 10, Sort.by("author").descending())
        )

        assertEquals(listOf("Cortazar", "Borges", "Asimov"), pageDesc.content.map { it.author })
    }

    @Test
    fun `findMyBooks ordena por title asc`() {
        book1.title = "C title"
        book3.title = "A title"
        book5.title = "B title"
        bookRepository.saveAll(listOf(book1, book3, book5))

        val page = bookRepository.findMyBooks(
            publisherId = user1.id!!,
            filter = BOOKFILTER.ALL.value,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            pageable = PageRequest.of(0, 10, Sort.by("title").ascending())
        )

        assertEquals(listOf("A title", "B title", "C title"), page.content.map { it.title })
    }

    @Test
    fun `findFilteredBooks ordena por publisher asc`() {
        val page = bookRepository.findFilteredBooks(
            title = null,
            isbn = null,
            genres = null,
            maxPages = null,
            minPages = null,
            username = null,
            fromDate = null,
            toDate = null,
            pageable = PageRequest.of(0, 10, Sort.by("userPublisher.name").ascending())
        )

        val names = page.content.map { it.userPublisher.name }
        assertEquals(listOf("User1", "User1", "User1", "Z_publisher", "Z_publisher"), names)
    }

    private fun initEntities() {
        user1 = userRepository.save(
            User(
                "User1", "Demaino", "1234", "frandemaino@gmail.com",
                "4789075423", "Me gusta leer libros de ciencia ficción y fantasía.",
                "Buenos Aires", mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER), 0
            )
        )

        user2 = userRepository.save(
            User(
                "Z_publisher", "", "1234", "admin@gmail.com",
                "10000000", "Soy el dios de la aplicación!!!",
                "adminlandia", mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER), 1000
            )
        )

        user3 = userRepository.save(
            User(
                "Z_user", "", "1234", "z_user@gmail.com",
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
        bookRepository.save(book5)
    }
}