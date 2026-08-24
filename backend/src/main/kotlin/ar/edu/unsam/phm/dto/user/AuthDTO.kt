package ar.edu.unsam.phm.dto.user

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginRequest(val email: String, val password: String)
data class TokenResponseDTO(val token: String, val refreshToken: String)
data class RefreshTokenRequestDTO(
    @JsonProperty("refreshToken")
    val refreshToken: String
)