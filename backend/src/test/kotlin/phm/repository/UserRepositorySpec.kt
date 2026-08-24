package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.Optional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate

@DataJpaTest
class UserRepositorySpec {

    @Autowired lateinit var userRepository: UserRepository
    lateinit var userA: User
    lateinit var userB: User

    @BeforeEach
    fun setUp() {
        userA = createUser("juan", "Al@gmail", "abc123")
        userB = createUser("nacho", "Zx@gmail", "xyz789")
        userRepository.save(userA)
        userRepository.save(userB)
    }

    private fun createUser(nombre: String, email: String, password: String): User {
        return User(
            name = nombre,
            lastname = "Perez",
            email = email,
            password = password,
            bibliokarma = 500,
            phone = "1127727722",
            description = "Yo soy un test dummy",
            residenceCity = "Brasil",
            type = mutableSetOf(USER_TYPE.PUBLISHER),
            createdAt = LocalDate.now(),
        )
    }

    @Test
    fun `findByEmail retorna un usuario si encuentra uno con el mismo email`() {
        assertEquals("juan", userRepository.findByEmail("Al@gmail").get().name)
    }

    @Test
    fun `findByEmail retorna Optional vacio si no encuentra usuario`() {
        assertEquals(Optional.empty<User>(), userRepository.findByEmail("nothereuseremail@gmail"))
    }
}