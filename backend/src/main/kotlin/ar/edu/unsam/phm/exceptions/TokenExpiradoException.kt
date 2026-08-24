package ar.edu.unsam.phm.exceptions

class TokenExpiradoException(mensaje: String = "Token vencido") : RuntimeException(mensaje)