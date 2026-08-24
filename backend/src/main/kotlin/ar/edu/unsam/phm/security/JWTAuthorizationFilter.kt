package ar.edu.unsam.phm.security

import ar.edu.unsam.phm.exceptions.TokenExpiradoException
import ar.edu.unsam.phm.services.AuthService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.text.startsWith
import kotlin.text.substringAfter

@Component
class JWTAuthorizationFilter: OncePerRequestFilter() {
    @Autowired
    lateinit var tokenUtils: TokenUtils
    @Autowired
    lateinit var authService: AuthService

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val bearerToken = request.getHeader("Authorization")
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                val token = bearerToken.substringAfter("Bearer ")
                val authentication = tokenUtils.getAuthentication(token)
                val principal = authentication.principal as Pair<Int, String>
                val email = principal.second
                authService.validarUsuario(email)
                SecurityContextHolder.getContext().authentication = authentication
                logger.info("username PAT: $authentication")
            }
            filterChain.doFilter(request, response)
        } catch (e: TokenExpiradoException) {
            logger.warn(e.message)
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\", error_description=\"The access token expired\"")
            response.contentType = "application/json"
            response.writer.write("{\"error\":\"Token expired\",\"message\":\"${e.message}\"}")
            return
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/api/auth/")
    }
}