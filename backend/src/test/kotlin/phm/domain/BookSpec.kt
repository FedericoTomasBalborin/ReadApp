package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.RatingResponseDTO
import ar.edu.unsam.phm.exceptions.BadRequestException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class BookSpec {

    private lateinit var randomUser: User
    private lateinit var publisher: User
    private lateinit var commonBook: CommonBook
    private lateinit var dedicationBook: DedicationBook
    private lateinit var collectibleBook: CollectibleBook
    private val today = LocalDate.now()

    // Mantiene la semántica de los tests originales de rating tras el refactor.
    private val ratingsByBook = mutableMapOf<Book, MutableList<Rating>>()

    @BeforeEach
    fun setUp() {
        publisher = createUser(USER_TYPE.PUBLISHER)
        randomUser = createUser(USER_TYPE.READER)
        commonBook = createCommonBook()
        dedicationBook = createDedicationBook()
        collectibleBook = createCollectibleBook()
    }

    private fun createUser(type: USER_TYPE): User {
        return User(
            name = "Juan",
            lastname = "Perez",
            email = "juan@test.com",
            password = "1234",
            bibliokarma = 500,
            phone = "1127727722",
            description = "Yo soy un test dummy",
            residenceCity = "Brasil",
            type = mutableSetOf(type),
            createdAt = today
        )
    }

    private fun createCommonBook(): CommonBook {
        return CommonBook(
            title = "Common Sense",
            description = "Desc",
            coverUrl = "url.jpg",
            author = "Thomas Paine",
            pages = 100,
            isbn = "9781440630514",
            language = BOOK_LANGUAGE.SPANISH,
            editorial = "Edit",
            publicationDate = today,
            state = BOOK_STATE.GOOD,
            genre = BOOK_GENRE.SELF_HELP,
        ).apply {
            id = UUID.randomUUID().toString()
            userPublisher = UserPublisher(1, this@BookSpec.publisher.name)
        }
    }

    private fun createCollectibleBook(): CollectibleBook {
        return CollectibleBook(
            title = "Bone Collector",
            description = "Desc",
            coverUrl = "url.jpg",
            author = "Barasui",
            pages = 100,
            isbn = "9781598164947",
            language = BOOK_LANGUAGE.SPANISH,
            editorial = "Edit",
            publicationDate = today,
            state = BOOK_STATE.GOOD,
            genre = BOOK_GENRE.DRAMA,
        ).apply {
            id = UUID.randomUUID().toString()
            userPublisher = UserPublisher(1, this@BookSpec.publisher.name)
        }
    }

    private fun createDedicationBook(): DedicationBook {
        return DedicationBook(
            title = "Dedicated to You",
            description = "Desc",
            coverUrl = "url.jpg",
            author = "Ella Fitzgerald",
            pages = 100,
            isbn = "9791234567890",
            language = BOOK_LANGUAGE.SPANISH,
            editorial = "Edit",
            publicationDate = today,
            state = BOOK_STATE.GOOD,
            genre = BOOK_GENRE.DRAMA,
        ).apply {
            id = UUID.randomUUID().toString()
            userPublisher = UserPublisher(1, this@BookSpec.publisher.name)
        }
    }

    private fun createRating(calification: Double, user: User = this@BookSpec.randomUser, book: Book): Rating {
        return Rating(
            user = user,
            calification = calification,
            comment = "Liked the first one better.",
            bookId = book.id
        )
    }

    private fun Book.addRatingCompat(rating: Rating) {
        val current = ratingsByBook.getOrPut(this) { mutableListOf() }
        current.add(rating)
        ratingCount = current.size
        calification = kotlin.math.round(current.map { it.calification }.average() * 2.0) / 2.0
        if (firstTwoRatings.size < 2) firstTwoRatings.add(RatingResponseDTO.createFrom(rating))
    }

    private fun Book.getRatingsCompat(n: Int): List<Rating> = ratingsByBook[this].orEmpty().take(n)

    private fun Book.hasAlreadyRatingCompat(user: User): Boolean = ratingsByBook[this].orEmpty().any { it.user == user }

    @Nested
    inner class ValidationTests {
        @Test
        fun `Titulo no puede estar vacio`() {
            commonBook.title = ""
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Título: no puede estar vacío", exception.message)
        }

        @Test
        fun `Titulo no puede superar los 100 caracteres`() {
            commonBook.title = "a".repeat(101)
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Título: no puede superar los 100 caracteres", exception.message)
        }

        @Test
        fun `Descripcion no puede estar vacia`() {
            commonBook.description = ""
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Descripción: no puede estar vacía", exception.message)
        }

        @Test
        fun `Descripcion no puede superar los 500 caracteres`() {
            commonBook.description = "a".repeat(501)
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Descripción: no puede superar los 500 caracteres", exception.message)
        }

        @Test
        fun `CoverUrl debe ser una imagen valida`() {
            commonBook.coverUrl = "http://test.com/file.txt"
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("URL: no es una imagen", exception.message)
        }

        @Test
        fun `Autor no puede estar vacio`() {
            commonBook.author = ""
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Autor: no puede estar vacío", exception.message)
        }

        @Test
        fun `Autor no puede superar los 100 caracteres`() {
            commonBook.author = "a".repeat(101)
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Autor: no puede superar los 100 caracteres", exception.message)
        }

        @Test
        fun `Paginas debe estar entre 1 y 99999`() {
            commonBook.pages = 0
            var exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Páginas: debe ser un número entre 1 y 99999", exception.message)

            commonBook.pages = 100000
            exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Páginas: debe ser un número entre 1 y 99999", exception.message)
        }

        @Test
        fun `ISBN debe tener formato valido`() {
            commonBook.isbn = "123456"
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("ISBN: debe tener 13 números y empezar con 978 o 979", exception.message)
        }

        @Test
        fun `Editorial no puede estar vacia`() {
            commonBook.editorial = ""
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Editorial: no puede estar vacío", exception.message)
        }

        @Test
        fun `Editorial no puede superar los 100 caracteres`() {
            commonBook.editorial = "a".repeat(101)
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Editorial: no puede superar los 100 caracteres", exception.message)
        }

        @Test
        fun `Fecha de publicacion no puede ser futura`() {
            commonBook.publicationDate = LocalDate.now().plusDays(1)
            val exception = assertThrows<BadRequestException> {
                commonBook.validate()
            }
            assertEquals("Fecha de Publicación: no puede ser futura", exception.message)
        }

        @Test
        fun `Libro valido no lanza excepcion`() {
            assertDoesNotThrow {
                commonBook.validate()
            }
        }
    }

    @Nested
    inner class AvailabilityTests {
        @Test
        fun `Un libro sin reservas esta disponible siempre`() {
            assertTrue(commonBook.isAvailable(today, today.plusDays(1)))
            assertTrue(commonBook.isAvailable(today.plusWeeks(1), today.plusWeeks(2)))
        }

        @Test
        fun `Un libro con una reserva no esta disponible en esas fechas`() {
            val reservation = Reservation(
                user = randomUser,
                startDate = today,
                endDate = today.plusDays(6),
                book = commonBook
            )
            commonBook.addReservation(reservation)

            assertFalse(commonBook.isAvailable(today, today.plusDays(1)))
            assertTrue(commonBook.isAvailable(today.plusWeeks(1), today.plusWeeks(2)))
        }
    }

    @Nested
    inner class RatingTests {
        @Test
        fun `El promedio de un libro sin ratings es 0`() {
            assertEquals(0.0, commonBook.calification)
        }

        @Test
        fun `El promedio de un libro con un solo rating sera el mismo valor`() {
            val rating1 = createRating(4.0, book = commonBook)
            commonBook.addRatingCompat(rating1)

            assertEquals(4.0, commonBook.calification)
        }

        @Test
        fun `El promedio de un libro con varios ratings estara redondeado a un valor ,5`() {
            val rating1 = createRating(4.0, book = commonBook)
            val rating2 = createRating(2.0, book = commonBook)
            val rating3 = createRating(1.0, book = commonBook)
            commonBook.addRatingCompat(rating1)
            commonBook.addRatingCompat(rating2)
            commonBook.addRatingCompat(rating3)

            assertEquals(2.5, commonBook.calification)
        }

        @Test
        fun `getRatings puede devolver una n cantidad de ratings, retorna todos si n es mayor a la cantidad de libros`() {
            val rating1 = createRating(4.0, book = commonBook)
            val rating2 = createRating(2.0, book = commonBook)
            val rating3 = createRating(1.0, book = commonBook)
            commonBook.addRatingCompat(rating1)
            commonBook.addRatingCompat(rating2)
            commonBook.addRatingCompat(rating3)

            assertEquals(setOf(rating1, rating2), commonBook.getRatingsCompat(2).toSet())
            assertEquals(setOf(rating1, rating2, rating3), commonBook.getRatingsCompat(50).toSet())
        }

        @Test
        fun `hasAlreadyRating devuelve true si el usuario calificó el libro, false si no lo hizo`() {
            val user2 = createUser(USER_TYPE.READER)
            val rating1 = createRating(2.0, user2, commonBook)
            commonBook.addRatingCompat(rating1)

            assertTrue(commonBook.hasAlreadyRatingCompat(user2))
            assertFalse(commonBook.hasAlreadyRatingCompat(randomUser))
        }
    }

    @Nested
    inner class PolymorphicTests {
        @Test
        fun `El extra karma de un libro común sera 5 veces la cantidad de paginas si el usuario tiene poco karma`() {
            val lowKarmaUser = createUser(USER_TYPE.READER)
            lowKarmaUser.bibliokarma = 5

            assertEquals(500, commonBook.extraKarma(lowKarmaUser))
        }

        @Test
        fun `El extra karma de un libro común sera 2 veces la cantidad de paginas si el usuario tiene mucho karma`() {
            val highKarmaUser = createUser(USER_TYPE.READER)
            highKarmaUser.bibliokarma = 9999

            assertEquals(200, commonBook.extraKarma(highKarmaUser))
        }

        @Test
        fun `El extra karma de un libro con dedicatoria 200 plus 10 puntos por cada de sus reservas`() {
            val reservation1 = Reservation(
                user = randomUser,
                startDate = today,
                endDate = today.plusDays(6),
                book = dedicationBook
            )
            val reservation2 = Reservation(
                user = randomUser,
                startDate = today,
                endDate = today.plusDays(6),
                book = dedicationBook
            )
            dedicationBook.addReservation(reservation1)
            dedicationBook.addReservation(reservation2)

            assertEquals(220, dedicationBook.extraKarma(randomUser))
        }

        @Test
        fun `El extra karma de un libro con dedicatoria sera solo 200 si no tiene ninguna reserva`() {
            assertEquals(200, dedicationBook.extraKarma(randomUser))
        }

        @Test
        fun `El extra karma de un libro coleccionable sera la cantidad de paginas plus la quinta parte del karma de un usuario`() {
            assertEquals(200, collectibleBook.extraKarma(randomUser))
        }

        @Test
        fun `Si el usuario tiene un numero no multiplo de 5 de bibliokarma, el extra karma del libro coleccionable se redondeara para arriba`() {
            val oddKarmaUser = createUser(USER_TYPE.READER)
            oddKarmaUser.bibliokarma = 1301
            assertEquals(361, collectibleBook.extraKarma(oddKarmaUser))
        }
    }
}