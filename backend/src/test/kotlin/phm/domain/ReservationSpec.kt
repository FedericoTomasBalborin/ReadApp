package ar.edu.unsam.phm.domain

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

class ReservationSpec {

    private lateinit var user: User
    private lateinit var book: CommonBook
    private lateinit var reservation: Reservation
    private val today = LocalDate.now()

    @BeforeEach
    fun setUp() {
        val publisher = createUser("Mike", "Ehrmantraut", "mikehrm@gmail.com", mutableSetOf(USER_TYPE.PUBLISHER))
        book = createBook(publisher)
        user = createUser("Jane", "Doe", "info@gmail.com", mutableSetOf(USER_TYPE.READER))
    }

    @Nested
    inner class DaysTests {
        @Test
        fun `days retorna cero cuando inicio y fin son el mismo dia`() {
            reservation = Reservation(user, today, today, book)

            assertEquals(0, reservation.days())
        }

        @Test
        fun `days retorna la cantidad correcta de dias entre fechas`() {
            val reservation = Reservation(user, today, today.plusDays(6), book)

            assertEquals(6, reservation.days())
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `Si una reserva tiene como fecha de inicio una fecha anterior al dia de hoy, arrojara una excepcion de BadRequest`() {
            val reservation = Reservation(user, today.minusDays(7), today.plusMonths(10), book)

            val exception = assertThrows<BadRequestException> {
                reservation.validate()
            }

            assertEquals("La fecha no puede ser previa al dia actual", exception.message)
        }

        @Test
        fun `Si una reserva tiene como fecha de devolucion una anterior a la fecha de inicio, arrojara una excepcion de BadRequest`() {
            val reservation = Reservation(user, today.plusMonths(10), today.plusMonths(3), book)
            val exception = assertThrows<BadRequestException> {
                reservation.validate()
            }
            assertEquals("La fecha de devolucion no puede ser anterior a la fecha de pedido", exception.message)
        }

        @Test
        fun `Si una reserva tiene como fecha de inicio una fecha igual o posterior al dia de hoy y una fecha de devolucion posterior o igual a la fecha de inicio, no se arroja ninguna excepcion`() {
            val reservation = Reservation(user, today, today.plusMonths(10), book)
            assertDoesNotThrow {
                reservation.validate()
            }
        }
    }

    @Nested
    inner class CalculateKarmaTests {
        @Test
        fun `el valor de bibliokarmaLog sera equivalente al karma del libro plus 5 por cada dia de reserva`() {
            val reservation = Reservation(user, today, today.plusDays(6), book)

            assertEquals(1530, reservation.bibliokarmaLog)
        }
    }

    @Nested
    inner class OverlappingTests {
        @Test
        fun `isOverlapping retorna true cuando los periodos se superponen`() {
            val reservation = Reservation(user, today, today.plusDays(6), book)
            assertTrue(reservation.isOverlapping(today.plusDays(2), today.plusDays(8)))
        }

        @Test
        fun `isOverlapping retorna true cuando la nueva reserva toca el borde`() {
            val reservation = Reservation(user, today, today.plusDays(6), book)
            assertTrue(reservation.isOverlapping(today.plusDays(6), today.plusDays(10)))
        }

        @Test
        fun `isOverlapping retorna false cuando los periodos no se cruzan`() {
            val reservation = Reservation(user, today, today.plusDays(6), book)
            assertFalse(reservation.isOverlapping(today.plusDays(7), today.plusDays(10)))
        }
    }

    @Nested
    inner class StateTests {
        @Test
        fun `getState retorna activo cuando faltan mas de dos dias para terminar`() {
            val reservation = Reservation(user, today, today.plusDays(4), book)

            assertEquals(RESERVATION_STATE.ACTIVE.frontName, reservation.getState())
        }

        @Test
        fun `getState retorna proximo a vencer cuando faltan dos dias o menos`() {
            val reservation = Reservation(user, today, today.plusDays(1), book)

            assertEquals(RESERVATION_STATE.NEAR_EXPIRATION.frontName, reservation.getState())
        }

        @Test
        fun `getState retorna devuelto cuando la fecha de fin ya paso`() {
            val reservation = Reservation(user, today.minusDays(2), today.minusDays(1), book)

            assertEquals(RESERVATION_STATE.RETURNED.frontName, reservation.getState())
        }
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

    private fun createBook(publisher: User): CommonBook {
        return CommonBook(
            title = "Dune Messiah",
            description = "Descripcion de prueba",
            coverUrl = "https://example.com/cover.jpg",
            author = "Frank Herbert",
            pages = 300,
            isbn = "9781440630514",
            language = BOOK_LANGUAGE.ENGLISH,
            editorial = "Editorial de prueba",
            publicationDate = LocalDate.of(2000, 1, 1),
            state = BOOK_STATE.EXCELLENT,
            genre = BOOK_GENRE.SCIENCE_FICTION,
        ).apply {
            id = UUID.randomUUID().toString()
            userPublisher = UserPublisher(1, publisher.name)
        }
    }
}