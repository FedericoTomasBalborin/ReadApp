package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.exceptions.BadRequestException
import ar.edu.unsam.phm.exceptions.InvalidCredentialsException
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

@Entity
@Table(name = "users")
class User(

    @Column(nullable = false) // Revisar cómo valida
    var name: String,
    var lastname: String,
    var password: String,
    var email: String,
    var phone: String,
    var description: String,
    var residenceCity: String,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    var type: MutableSet<USER_TYPE> = mutableSetOf(),
    var bibliokarma: Int = 0,
    val createdAt: LocalDate = LocalDate.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null


    var isActive: Boolean = true // Averiguar si el framework puede gestionar esto internamente

    fun validateRequiredFields() {
        if (name.isBlank()) throw BadRequestException("El nombre no puede ser vacio")
        if (lastname.isBlank()) throw BadRequestException("El apellido no puede ser vacio")
        if (password.isBlank()) throw BadRequestException("La contraseña no puede ser vacia")
        if (email.isBlank()) throw BadRequestException("El email no puede ser vacio")
    }

    fun validateUpdateFields() {
        validateRequiredFields()
        if (phone.isBlank()) throw BadRequestException("El numero de telefono no puede ser vacio")
        if (phone.length != 10) throw BadRequestException("El numero de telefono debe tener 10 digitos")
        if (description.isBlank()) throw BadRequestException("La descripción no puede ser vacia")
        if (description.length > 255) throw BadRequestException("La descripción no puede tener mas de 255 caracteres")
        if (residenceCity.isBlank()) throw BadRequestException("La ciudad de residencia no puede ser vacia")
        if (bibliokarma < 0) throw BadRequestException("El bibliokarma no puede ser negativo")
    }

    fun earnBibliokarmaPoints(points: Int) {
        if (points < 0) throw BadRequestException("No puede sumar bibliokarma con valores negativos")
        this.bibliokarma += points
    }

    fun validarCredenciales(passwordAVerificar: String) {
        if (!getDefaultEncoder().matches(passwordAVerificar, password)) {
            throw InvalidCredentialsException()
        }
    }

    private fun getDefaultEncoder(): PasswordEncoder {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()!!
    }
}