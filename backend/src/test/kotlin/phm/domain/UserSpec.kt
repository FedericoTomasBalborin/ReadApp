package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.exceptions.BadRequestException
import ar.edu.unsam.phm.exceptions.InvalidCredentialsException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.time.LocalDate

class UserSpec {

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = User(
            name = "Juan",
            lastname = "Perez",
            email = "juan@test.com",
            password = "1234",
            bibliokarma = 100,
            phone = "1127727722",
            description = "Usuario de prueba",
            residenceCity = "Buenos Aires",
            type = mutableSetOf(USER_TYPE.READER),
            createdAt = LocalDate.now()
        )
    }

    @Nested
    inner class BibliokarmaPointsTests {
        @Test
        fun `earnBibliokarmaPoints suma bibliokarmas al usuario`() {
            assertEquals(100, user.bibliokarma)
            user.earnBibliokarmaPoints(20)
            assertEquals(120, user.bibliokarma)
        }

        @Test
        fun `earnBibliokarmaPoints no suma bibliokarmas negativos y tira una excepcion`() {
            val exception = assertThrows<BadRequestException> {
                user.earnBibliokarmaPoints(-20)
            }
            assertEquals("No puede sumar bibliokarma con valores negativos", exception.message)
            assertEquals(100, user.bibliokarma)
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `el usuario no puede tener vacio su nombre` () {
            user.name = ""
            val exception = assertThrows<BadRequestException> {
                user.validateRequiredFields()
            }
            assertEquals("El nombre no puede ser vacio", exception.message)
        }

        @Test
        fun `el usuario no puede tener vacio su apellido` () {
            user.lastname = ""
            val exception = assertThrows<BadRequestException> {
                user.validateRequiredFields()
            }
            assertEquals("El apellido no puede ser vacio", exception.message)
        }
        @Test
        fun `el usuario no puede tener vacio su email` () {
            user.email = ""
            val exception = assertThrows<BadRequestException> {
                user.validateRequiredFields()
            }
            assertEquals("El email no puede ser vacio", exception.message)
        }
        @Test
        fun `el usuario no puede tener vacia su contraseña` () {
            user.password = ""
            val exception = assertThrows<BadRequestException> {
                user.validateRequiredFields()
            }
            assertEquals("La contraseña no puede ser vacia", exception.message)
        }
    }
    @Test
    fun `el usuario no puede tener vacio su numero de telefono`() {
        user.phone = ""
        val exception = assertThrows<BadRequestException> {
            user.validateUpdateFields()
        }
        assertEquals("El numero de telefono no puede ser vacio", exception.message)
    }

    @Test
    fun `el numero de telefono debe tener 10 digitos`() {
        user.phone = "123"
        val exception = assertThrows<BadRequestException> {
            user.validateUpdateFields()
        }
        assertEquals("El numero de telefono debe tener 10 digitos", exception.message)
    }

    @Test
    fun `el usuario no puede tener vacia su descripcion`() {
        user.description = ""
        val exception = assertThrows<BadRequestException> {
            user.validateUpdateFields()
        }
        assertEquals("La descripción no puede ser vacia", exception.message)
    }

    @Test
    fun `la descripcion no puede superar los 255 caracteres`() {
        user.description = "a".repeat(256)
        val exception = assertThrows<BadRequestException> {
            user.validateUpdateFields()
        }
        assertEquals("La descripción no puede tener mas de 255 caracteres", exception.message)
    }

    @Test
    fun `el usuario no puede tener vacia su ciudad de residencia`() {
        user.residenceCity = ""
        val exception = assertThrows<BadRequestException> {
            user.validateUpdateFields()
        }
        assertEquals("La ciudad de residencia no puede ser vacia", exception.message)
    }

    @Test
    fun `el bibliokarma no puede ser negativo`() {
        user.bibliokarma = -1
        val exception = assertThrows<BadRequestException> {
            user.validateUpdateFields()
        }
        assertEquals("El bibliokarma no puede ser negativo", exception.message)
    }

    @Test
    fun `validateUpdateFields pasa correctamente con datos validos`() {
        assertDoesNotThrow {
            user.validateUpdateFields()
        }
    }

    @Nested
    inner class CredentialsTests {

        @Test
        fun `validarCredenciales no lanza excepcion con password correcta`() {
            val encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
            user.password = encoder.encode("1234")

            assertDoesNotThrow {
                user.validarCredenciales("1234")
            }
        }

        @Test
        fun `validarCredenciales lanza excepcion con password incorrecta`() {
            val encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
            user.password = encoder.encode("1234")

            assertThrows<InvalidCredentialsException> {
                user.validarCredenciales("tukitakiloremipsumyo")
            }
        }
    }
}