package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.RatingRequestDTO
import ar.edu.unsam.phm.exceptions.BadRequestException
import jakarta.persistence.*

@Entity
@Table(name = "ratings")
class Rating(
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,
    var calification: Double,
    var comment: String,
    val bookId: String
) {
    fun validate() {
        if (calification !in 1.0..5.0) throw BadRequestException("La calificacion debe ser un numero entre 1 y 5")
        if (comment.isBlank()) throw BadRequestException("El comentario no puede estar vacio")
        if (comment.length > 255) throw BadRequestException("El comentario no puede tener mas de 255 caracteres")
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var isActive: Boolean = true

    companion object {
        fun buildRating(ratingReqDTO: RatingRequestDTO, user: User): Rating {
            return Rating(
                user = user,
                calification = ratingReqDTO.calification,
                comment = ratingReqDTO.comment,
                bookId = ratingReqDTO.idBook
            )
        }
    }

}