package ar.edu.unsam.phm.domain

import com.fasterxml.jackson.annotation.JsonCreator
import ar.edu.unsam.phm.exceptions.BadRequestException

enum class BOOK_LANGUAGE(val frontName: String) {
    SPANISH("Español"),
    ENGLISH("Inglés"),
    FRENCH("Francés"),
    PORTUGUESE("Portugués");

    companion object {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        @JvmStatic
        fun fromFrontName(frontName: String): BOOK_LANGUAGE {
            return entries.firstOrNull { it.frontName == frontName }
                ?: throw BadRequestException("Invalid BOOK_LANGUAGE: $frontName")
        }
    }
}
