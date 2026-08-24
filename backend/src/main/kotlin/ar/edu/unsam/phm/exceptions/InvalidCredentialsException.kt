package ar.edu.unsam.phm.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidCredentialsException(mensaje: String = "Las credenciales son inválidas") : RuntimeException(mensaje)