package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.exceptions.BadRequestException
import com.fasterxml.jackson.annotation.JsonCreator


enum class BOOK_GENRE(val frontName: String) {
    DRAMA("Drama"),
    SCIENCE_FICTION("Ciencia ficción"),
    ROMANCE("Romance"),
    SELF_HELP("Autoayuda"),
    DESIGN("Diseño"),
    CLASSIC_LITERATURE("Literatura clásica");

    companion object {

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        @JvmStatic
        fun fromFrontName(frontName: String): BOOK_GENRE {
            return entries.firstOrNull { it.frontName == frontName }
                ?: throw BadRequestException("Invalid BOOK_GENRE: $frontName")
        }
    }
}
