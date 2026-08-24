package ar.edu.unsam.phm.domain

import com.fasterxml.jackson.annotation.JsonCreator
import ar.edu.unsam.phm.exceptions.BadRequestException

enum class BOOK_STATE(val frontName: String) {
    BAD("Malo"),
    REGULAR("Regular"),
    GOOD("Bueno"),
    VERY_GOOD("Muy bueno"),
    EXCELLENT("Excelente");

    companion object {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        @JvmStatic
        fun fromFrontName(frontName: String): BOOK_STATE {
            return entries.firstOrNull { it.frontName == frontName }
                ?: throw BadRequestException("Invalid BOOK_STATE: $frontName")
        }
    }
}