package ar.edu.unsam.phm.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(unique = true, nullable = false)
    var tokenHash: String = ""

    @Column(nullable = false)
    var email: String = ""

    @Column(nullable = false)
    var expirationDate: LocalDateTime = LocalDateTime.now().plusHours(1)

    var revoked: Boolean = false

    fun isValid(): Boolean = !revoked && LocalDateTime.now().isBefore(expirationDate)
}