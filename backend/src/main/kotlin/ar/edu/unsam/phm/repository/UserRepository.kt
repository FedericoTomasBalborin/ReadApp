package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface UserRepository : JpaRepository<User, Int> {

    interface UserKarmaProjection {
        fun getName(): String
        fun getLastname(): String
        fun getBibliokarma(): Int
    }

    fun findByEmail(email: String): Optional<User>

    @Query(
        value = """
        SELECT name, lastname, bibliokarma
        FROM users
        ORDER BY bibliokarma DESC
        LIMIT 5
    """,
        nativeQuery = true
    )
    fun findTop5UsersHighestBibliokarma(): List<UserKarmaProjection>
}