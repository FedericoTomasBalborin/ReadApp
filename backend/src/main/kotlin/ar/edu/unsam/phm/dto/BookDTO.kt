package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.BOOK_GENRE
import ar.edu.unsam.phm.domain.BOOK_LANGUAGE
import ar.edu.unsam.phm.domain.BOOK_STATE
import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.CachedBook
import java.time.LocalDate

data class FilteredBooksDTO(
    val books: List<BookCardDTO>, val totalFilteredBooks: Long
)

data class MyBooksResponse(
    val books: List<BookRowDTO>, val totalSize: Long
)

data class BookFiltersDTO(
    val genres: List<BOOK_GENRE>? = null,
    val maxPages: Int? = null,
    val minPages: Int? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val isbn: String? = null,
    val username: String? = null,
    val order: ORDER? = null,
    val title: String? = null,
    val isFilter: Boolean
)

data class BookCardDTO(
    val id: String,
    val title: String,
    val author: String,
    val rating: Double,
    val coverUrl: String,
    val isbn: String,
    val language: String,
    val status: String,
    val publisher: String,
    val genre: String
) {
    companion object {
        fun createFrom(book: Book, publisherName: String): BookCardDTO {
            return BookCardDTO(
                id = book.id,
                title = book.title,
                author = book.author,
                rating = book.calification,
                coverUrl = book.coverUrl,
                isbn = book.isbn,
                language = book.language.frontName,
                status = book.state.frontName,
                publisher = publisherName,
                genre = book.genre.frontName
            )
        }

        fun createFrom(book: CachedBook): BookCardDTO {
            return BookCardDTO(
                id = book.id,
                title = book.title,
                author = book.author,
                rating = book.calification,
                coverUrl = book.coverUrl,
                isbn = book.isbn,
                language = book.language.frontName,
                status = book.state.frontName,
                publisher = book.publisherName,
                genre = book.genre.frontName
            )
        }

        fun toCachedBook(bookCardDTO: BookCardDTO): CachedBook {
            return CachedBook(
                id = bookCardDTO.id,
                title = bookCardDTO.title,
                coverUrl = bookCardDTO.coverUrl,
                author = bookCardDTO.author,
                isbn = bookCardDTO.isbn,
                language = BOOK_LANGUAGE.fromFrontName(bookCardDTO.language),
                state = BOOK_STATE.fromFrontName(bookCardDTO.status),
                genre = BOOK_GENRE.fromFrontName(bookCardDTO.genre),
                calification = bookCardDTO.rating,
                publisherName = bookCardDTO.publisher
            )
        }
    }
}

data class BookClicks(
    val bookId: String,
    val clicks: Int
)

data class BookRowDTO(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val genre: String,
    val isAvailable: Boolean,
    val isActive: Boolean,
    val addedDate: String,
    val clickCount: Int
) {
    companion object {
        fun createFrom(book: Book, isAvailable: Boolean, clickCount: Int?): BookRowDTO {
            return BookRowDTO(
                id = book.id,
                title = book.title,
                author = book.author,
                coverUrl = book.coverUrl,
                genre = book.genre.frontName,
                isAvailable = isAvailable,
                isActive = book.isActive,
                addedDate = book.publicationDate.toString(),
                clickCount = clickCount ?: 0
            )
        }
    }
}

data class BookDetailDTO(
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val author: String,
    val pages: Int,
    val isbn: String,
    val type: String,
    val language: String,
    val editorial: String,
    val publicationDate: LocalDate,
    val state: String,
    val genre: String, // REFACTOR FRONT
    val ratingAverage: Double,
    val ratingCount: Int,
    val firstTwoRatings: List<RatingResponseDTO>,
    val reservationsDates: List<ReservationDatesDTO>,
    val bibliokarma: Int
) {
    companion object {
        fun buildBookDetailDTO(
            book: Book,
            reservationsDates: List<ReservationDatesDTO>,
            ratingAverage: Double,
            ratingCount: Int,
            firstTwoRatings: List<RatingResponseDTO>,
            bibliokarma: Int
        ): BookDetailDTO {
            return BookDetailDTO(
                id = book.id,
                title = book.title,
                description = book.description,
                coverUrl = book.coverUrl,
                author = book.author,
                pages = book.pages,
                isbn = book.isbn,
                type = book.frontName(),
                language = book.language.frontName,
                editorial = book.editorial,
                publicationDate = book.publicationDate,
                state = book.state.frontName,
                genre = book.genre.frontName,
                ratingAverage = ratingAverage,
                ratingCount = ratingCount,
                firstTwoRatings = firstTwoRatings,
                reservationsDates = reservationsDates,
                bibliokarma = bibliokarma
            )
        }
    }
}

data class BibliokarmaDTO(val bibliokarma: Int)

enum class ORDER(
    val column: String
) {
    AUTHOR("author"), TITLE("title"), PUBLISHER("userPublisher.name"), ADDED_DATE("publicationDate"), BY_AVAILABILITY("currentlyBorrowed")
}

enum class BOOKFILTER{
    ALL, AVAILABLE, BORROWED, DELETED
}