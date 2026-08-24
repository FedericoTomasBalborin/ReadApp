package ar.edu.unsam.phm.dto.metrics

import ar.edu.unsam.phm.exceptions.BadRequestException

data class RatingByBookTypeDTO(
    val bookType: BookType,
    val averageRating: Double
)

enum class BookType(val frontName: String) {
    COMMON("ar.edu.unsam.phm.domain.CommonBook"),
    DEDICATION("ar.edu.unsam.phm.domain.DedicationBook"),
    COLLECTIBLE("ar.edu.unsam.phm.domain.CollectibleBook");

    companion object {
        fun fromFrontName(frontName: String): BookType {
            return BookType.entries.firstOrNull { it.frontName == frontName }
                ?: throw BadRequestException("Invalid BookType: $frontName")
        }
    }
}

data class RatingByBookTypeMongo(
    val type: String,
    val averageRating: Double
)