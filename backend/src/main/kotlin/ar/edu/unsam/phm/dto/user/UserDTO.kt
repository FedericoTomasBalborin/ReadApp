package ar.edu.unsam.phm.dto.user

import ar.edu.unsam.phm.domain.User

data class UserDTO(
    val id: Int?,
    val name: String,
    val lastname: String,
    val email: String,
    val phone: String,
    val description: String,
    val residenceCity: String,
    val type: Set<String>,
    val bibliokarma: Int,
    val createdAt: String,
    val sharedBooksCount: Int,
    val readBooksCount: Int,
) {
    companion object {
        fun createFrom(user: User, sharedBooksCount: Int, readBooksCount: Int): UserDTO {
            return UserDTO(
                id = user.id,
                name = user.name,
                lastname = user.lastname,
                email = user.email,
                phone = user.phone,
                description = user.description,
                residenceCity = user.residenceCity,
                type = user.type.map { it.frontName }.toMutableSet(),
                bibliokarma = user.bibliokarma,
                createdAt = user.createdAt.toString(),
                sharedBooksCount = sharedBooksCount,
                readBooksCount = readBooksCount
            )
        }
    }
}

data class EditUserDTO(
    val name: String,
    val lastname: String,
    val email: String,
    val phone: String,
    val description: String,
    val residenceCity: String,
    val roles: List<String>,
)

data class HeaderUserDTO(
    val lastname: String,
    val name: String,
    val bibliokarma: Int
) {
    companion object {
        fun createFrom(user: User): HeaderUserDTO {
            return HeaderUserDTO(
                lastname = user.lastname,
                name = user.name,
                bibliokarma = user.bibliokarma
            )
        }
    }
}