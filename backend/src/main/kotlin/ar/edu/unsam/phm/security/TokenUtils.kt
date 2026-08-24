package ar.edu.unsam.phm.security

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.exceptions.InvalidCredentialsException
import ar.edu.unsam.phm.exceptions.TokenExpiradoException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Date
import kotlin.time.Duration.Companion.minutes

@Component
class TokenUtils {
    @Value("\${security.secret-key}")
    lateinit var secretKey: String

    @Value("\${security.access-token-minutes}")
    var accessTokenMinutes: Int = 5

    @Value("\${security.refresh-token-minutes}")
    var refreshTokenMinutes: Int = 10

    val logger: Logger = LoggerFactory.getLogger(TokenUtils::class.java)

    fun createToken(user: User): String? {
        val longExpirationTime = accessTokenMinutes.minutes.inWholeMilliseconds
        val now = Date()

        return Jwts.builder()
            .subject(user.email)                                             // Quien sos
            .issuedAt(now)                                                   // Cuando se emitio
            .expiration(Date(now.time + longExpirationTime))                 // Cuando vence
            .claim("roles", user.type.map { it.frontName })             // Que podes hacer
            .claim("userId", user.id)
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()))   // Firma con secret key
            .compact()
    }

    fun generateRefreshToken(): String {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun getRefreshTokenExpirationMinutes(): Int = refreshTokenMinutes

    fun getHashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest((token + secretKey).toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }

    fun getAuthentication(token: String): UsernamePasswordAuthenticationToken {
        try {
            val secret = Keys.hmacShaKeyFor(secretKey.toByteArray())
            val claims = Jwts.parser()
                .verifyWith(secret)
                .build()
                // acá se valida el vencimiento del token
                .parseSignedClaims(token)
                .payload

            // Token no tiene usuario
            if (claims.subject == null || claims.subject.isBlank()) {
                throw InvalidCredentialsException()
            }

            logger.info("Token decoded, user: " + claims.subject + " - roles: " + claims["roles"])

            val userId = (claims["userId"] as Number).toInt()
            val email = claims.subject

            val principal = Pair(userId, email)

            val roles = (claims["roles"] as List<*>).map { SimpleGrantedAuthority(it.toString()) }
            return UsernamePasswordAuthenticationToken(principal, null, roles)
        } catch (expiredJwtException: ExpiredJwtException) {
            throw TokenExpiradoException("Sesión vencida")
        }
    }
}