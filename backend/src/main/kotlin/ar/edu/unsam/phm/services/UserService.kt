package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.USER_TYPE
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.user.EditUserDTO
import ar.edu.unsam.phm.dto.user.HeaderUserDTO
import ar.edu.unsam.phm.dto.user.NewUserRequestDTO
import ar.edu.unsam.phm.exceptions.BadRequestException
import ar.edu.unsam.phm.exceptions.InvalidCredentialsException
import ar.edu.unsam.phm.exceptions.NotFoundException
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.repository.UserRepository.UserKarmaProjection
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private val passwordEncoder: PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    @Transactional(rollbackFor = [Exception::class])
    fun createUser(newUserDTO: NewUserRequestDTO): User{
        val normalizedEmail = newUserDTO.email
        validateEmail(newUserDTO.email)
        val normalizedFullname = normalizeFullName(newUserDTO.fullName)
        val user = newUserDTOToUser(newUserDTO.copy(email = normalizedEmail, fullName = normalizedFullname))
        user.validateRequiredFields()
        return userRepository.save(user)
    }

    private fun newUserDTOToUser(newUserDTO: NewUserRequestDTO): User {
        val nameParts = newUserDTO.fullName.split(" ")

        return User(
            name = nameParts[0],
            lastname = nameParts.getOrElse(1) { "" },
            password = passwordEncoder.encode(newUserDTO.password),
            email = newUserDTO.email,
            phone = "0000000000",
            description = "",
            residenceCity = "",
            type = mutableSetOf(USER_TYPE.READER),
            bibliokarma = 0,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateUser(userId: Int, editUserDTO: EditUserDTO) : User {
        val user = findById(userId)
        update(user, editUserDTO)
        return userRepository.save(user)
    }

    private fun update(user: User, editUserDTO: EditUserDTO) {
        user.name = editUserDTO.name
        user.lastname = editUserDTO.lastname
        user.email = editUserDTO.email
        user.phone = editUserDTO.phone
        user.description = editUserDTO.description
        user.residenceCity = editUserDTO.residenceCity
        if (editUserDTO.roles.isEmpty()) {
            throw BadRequestException("El usuario debe tener al menos un rol")
        }
        user.type = editUserDTO.roles.map { USER_TYPE.fromFrontName(it) }.toMutableSet()
        user.validateUpdateFields()
    }

    private fun validateEmail(email: String) {
        if(userRepository.findByEmail(email).isPresent){
            throw BadRequestException("El email ya está en uso")
        }
    }

    private fun normalizeFullName(fullName: String): String {
        val normalized = fullName.trim().lowercase().replace("\\s+".toRegex(), " ")
        if (normalized.split(" ", limit = 2).size < 2) {
            throw BadRequestException("El nombre completo debe incluir nombre y apellido")
        }
        return normalized
    }

    fun getHeaderData(userId: Int): HeaderUserDTO {
        val user = findById(userId)
        return HeaderUserDTO.createFrom(user)
    }

    fun findById(id: Int) : User {
        return userRepository.findById(id).orElseThrow { NotFoundException("Usuario no encontrado") }
    }

    fun findByEmail(email: String) : User {
        return userRepository.findByEmail(email).orElseThrow { InvalidCredentialsException() }
    }

    fun findTop5UsersHighestBibliokarma(): List<UserKarmaProjection> {
        return userRepository.findTop5UsersHighestBibliokarma()
    }

    fun findAllByIdWithMapping(userIds: List<Int>): Map<Int, User> {
        return userRepository.findAllById(userIds).associateBy { it.id!! }
    }
}