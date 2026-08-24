package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.exceptions.BadRequestException

enum class USER_TYPE(val frontName: String) {
    READER("Lector"),
    PUBLISHER("Publicador"),
    ADMIN("Admin");

    companion object {
        fun fromFrontName(value: String): USER_TYPE =
            entries.find { it.frontName.equals(value.trim(), ignoreCase = true) }
                ?: throw BadRequestException("Rol inválido: $value")
    }
}

