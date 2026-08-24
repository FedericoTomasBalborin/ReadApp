package ar.edu.unsam.phm.dto.user

data class NewUserRequestDTO (
    val fullName: String,
    val email: String,
    val password: String
)