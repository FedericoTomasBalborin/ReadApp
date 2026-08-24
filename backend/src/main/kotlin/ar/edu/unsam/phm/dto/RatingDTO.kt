package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Rating

data class RatingRequestDTO(val idBook: String, val calification: Double, val comment: String)

data class RatingResponseDTO(
    val username: String,
    val calification: Double,
    val comment: String
){
    companion object {
        fun createFrom(rating: Rating): RatingResponseDTO {
            return RatingResponseDTO(
                username = rating.user.name,
                calification = rating.calification,
                comment = rating.comment
            )
        }
    }
}
