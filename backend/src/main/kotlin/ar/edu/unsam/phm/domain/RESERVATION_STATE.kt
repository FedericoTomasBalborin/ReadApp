package ar.edu.unsam.phm.domain

enum class RESERVATION_STATE(val frontName: String) {
    ACTIVE("Activo"),
    NEAR_EXPIRATION("Próximo a vencer"),
    RETURNED("Devuelto")
}