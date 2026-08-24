package ar.edu.unsam.phm.controllers

import ar.edu.unsam.phm.dto.user.LoginRequest
import ar.edu.unsam.phm.dto.user.RefreshTokenRequestDTO
import ar.edu.unsam.phm.dto.user.TokenResponseDTO
import ar.edu.unsam.phm.services.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): TokenResponseDTO {
        return authService.login(loginRequest)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody body: RefreshTokenRequestDTO): TokenResponseDTO {
        return authService.refreshAccessToken(body.refreshToken)
    }
}