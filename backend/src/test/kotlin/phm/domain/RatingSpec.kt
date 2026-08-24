package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.exceptions.BadRequestException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class RatingSpec {
    lateinit var rating: Rating

    @BeforeEach
    fun setUp() {
        val publisher = createUser()
        val book = createBook(publisher)
        val user = createUser()
        rating = Rating(user, 3.0, "algun comentario", book.id)
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `La reseña no puede tener menos de 1,0 punto`() {
            rating.calification = 0.0
            val exception = assertThrows<BadRequestException> {
                rating.validate()
            }
            assertEquals("La calificacion debe ser un numero entre 1 y 5", exception.message)
        }

        @Test
        fun `La reseña no puede tener mas de 5,0 punto`() {
            rating.calification = 5.1
            val exception = assertThrows<BadRequestException> {
                rating.validate()
            }
            assertEquals("La calificacion debe ser un numero entre 1 y 5", exception.message)
        }

        @Test
        fun `La reseña no puede tener un comentario vacio`() {
            rating.comment = ""
            val exception = assertThrows<BadRequestException> {
                rating.validate()
            }
            assertEquals("El comentario no puede estar vacio", exception.message)
        }

        @Test
        fun `La reseña no puede tener un comentario de más de 255 caracteres`() {
            rating.comment = "a".repeat(256)
            val exception = assertThrows<BadRequestException> {
                rating.validate()
            }
            assertEquals("El comentario no puede tener mas de 255 caracteres", exception.message)
        }

        @Test
        fun `Una reseña con un comentario de 255 caracteres pasa sin problemas`() {
            rating.comment = "a".repeat(255)
            assertDoesNotThrow {
                rating.validate()
            }
        }
    }

    private fun createUser(): User {
        return User(
            name = "name",
            lastname = "lastname",
            password = "1234",
            email = "email",
            phone = "1122334455",
            description = "Usuario de prueba",
            residenceCity = "Buenos Aires",
            type = mutableSetOf(USER_TYPE.READER, USER_TYPE.PUBLISHER),
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