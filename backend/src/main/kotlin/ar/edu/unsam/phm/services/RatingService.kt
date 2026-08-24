package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Rating
import ar.edu.unsam.phm.dto.RatingRequestDTO
import ar.edu.unsam.phm.dto.RatingResponseDTO
import ar.edu.unsam.phm.exceptions.BadRequestException
import ar.edu.unsam.phm.repository.RatingRepository
import org.springframework.data.domain.Limit
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.floor

@Service
class RatingService(
    val ratingRepository: RatingRepository,
    val userService: UserService,
    private val bookService: BookService
) {
    @Transactional(rollbackFor = [Exception::class])
    fun createRating(idUser: Int, ratingReqDTO: RatingRequestDTO) {
        val existingRatingOfThisBookByThisUser = ratingRepository.findByUserIdAndBookId(idUser, ratingReqDTO.idBook)
        if (existingRatingOfThisBookByThisUser != null) throw BadRequestException("El usuario con id $idUser ya califico el libro con id ${ratingReqDTO.idBook}")

        val user = userService.findById(idUser)
        val book = bookService.findById(ratingReqDTO.idBook)

        val rating = Rating.buildRating(ratingReqDTO, user)
        rating.validate()
        ratingRepository.save(rating)

        recalculateBookRatings(book, rating)
    }

    @Transactional(readOnly = true)
    fun getBookRatings(idBook: String): List<RatingResponseDTO> {
        val projection = ratingRepository.findByBookId(idBook, Limit.unlimited())
        return projection.map { RatingResponseDTO.createFrom(it) }
    }

    private fun recalculateBookRatings(book: Book, rating: Rating) {
        val ratingStats = ratingRepository.getRatingAverageByBookId(book.id)
        book.ratingCount = ratingStats.getCount()
        book.calification = floor(ratingStats.getAverage() * 2.0) / 2.0
        book.firstTwoRatings.takeIf { it.size < 2 } ?.add(RatingResponseDTO.createFrom(rating))
        bookService.saveBook(book)
    }
}