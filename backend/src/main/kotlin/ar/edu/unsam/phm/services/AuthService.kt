package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.RefreshToken
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.user.LoginRequest
import ar.edu.unsam.phm.dto.user.TokenResponseDTO
import ar.edu.unsam.phm.exceptions.InvalidCredentialsException
import ar.edu.unsam.phm.repository.RefreshTokenRepository
import ar.edu.unsam.phm.security.TokenUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthService(
    val userService: UserService,
    val refreshTokenRepository: RefreshTokenRepository
) {
    @Autowired
    lateinit var tokenUtils: TokenUtils

    @Transactional
    fun login(request: LoginRequest): TokenResponseDTO {
        val user = validarUsuario(request.email)
        user.validarCredenciales(request.password)
        return issueToken(user)
    }

    @Transactional
    fun issueToken(user: User): TokenResponseDTO {
        val accessToken = tokenUtils.createToken(user)
            ?: throw InvalidCredentialsException("Error generando access token")
        val refreshToken = createRefreshToken(user.email)
        return TokenResponseDTO(accessToken, refreshToken)
    }

    private fun createRefreshToken(email: String): String {
        val tokenString = tokenUtils.generateRefreshToken()
        val expirationMinutes = tokenUtils.getRefreshTokenExpirationMinutes()

        val refreshToken = RefreshToken().apply {
            tokenHash = tokenUtils.getHashToken(tokenString)
            this.email = email
            expirationDate = LocalDateTime.now().plusMinutes(expirationMinutes.toLong())
        }

        refreshTokenRepository.save(refreshToken)
        return tokenString
    }

    @Transactional
    fun refreshAccessToken(refreshTokenString: String): TokenResponseDTO {
        val tokenHash = tokenUtils.getHashToken(refreshTokenString)
        val refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { InvalidCredentialsException("Refresh token no encontrado") }

        if (!refreshToken.isValid()) {
            throw InvalidCredentialsException("Refresh token expirado o revocado")
        }

        // Revocamos el refresh token viejo por seguridad
        refreshToken.revoked = true
        refreshTokenRepository.save(refreshToken)

        val user = validarUsuario(refreshToken.email)
        return issueToken(user)
    }

    fun validarUsuario(email: String): User = userService.findByEmail(email)
}