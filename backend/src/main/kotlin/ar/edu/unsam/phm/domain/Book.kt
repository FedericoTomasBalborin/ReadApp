package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.RatingResponseDTO
import ar.edu.unsam.phm.exceptions.BadRequestException
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate
import kotlin.math.ceil

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "bookType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = CommonBook::class, name = "Común"),
    JsonSubTypes.Type(value = DedicationBook::class, name = "Con dedicatoria"),
    JsonSubTypes.Type(value = CollectibleBook::class, name = "Coleccionable")
)
@Document(collection = "libros")
abstract class Book(
    var title: String,
    var description: String,
    var coverUrl: String,
    var author: String,
    var pages: Int,
    var isbn: String,
    var language: BOOK_LANGUAGE,
    var editorial: String,
    var publicationDate: LocalDate,
    var state: BOOK_STATE,
    @Enumerated(EnumType.STRING)
    var genre: BOOK_GENRE,
) {

    @Id
    lateinit var id: String
    lateinit var userPublisher: UserPublisher

    var createdAt : Instant = Instant.now()

    var calification: Double = 0.0

    var ratingCount: Int = 0

    var reservationsCount: Int = 0

    var reservationDates: MutableSet<DateRange> = mutableSetOf()

    var isActive: Boolean = true

    var currentlyBorrowed: Boolean = false

    var firstTwoRatings: MutableList<RatingResponseDTO> = mutableListOf()

    fun validate() {
        if (title.isBlank()) throw BadRequestException("Título: no puede estar vacío")
        if (title.length > 100) throw BadRequestException("Título: no puede superar los 100 caracteres")
        if (description.isBlank()) throw BadRequestException("Descripción: no puede estar vacía")
        if (description.length > 500) throw BadRequestException("Descripción: no puede superar los 500 caracteres")
        val possibleURLEndings = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".avif", ".bmp", ".svg")
        if (!possibleURLEndings.any { coverUrl.trim().endsWith(it, ignoreCase = true) }) throw BadRequestException("URL: no es una imagen")
        if (runCatching { java.net.URI(coverUrl) }.isFailure) throw BadRequestException("URL: Inválido")
        if (author.isBlank()) throw BadRequestException("Autor: no puede estar vacío")
        if (author.length > 100) throw BadRequestException("Autor: no puede superar los 100 caracteres")
        if (pages !in 1..<99999) throw BadRequestException("Páginas: debe ser un número entre 1 y 99999")
        val normalized = isbn.replace("-", "").replace(" ", "")
        if (!normalized.matches(Regex("^(97[89])\\d{10}$"))) throw BadRequestException("ISBN: debe tener 13 números y empezar con 978 o 979")
        if (editorial.isBlank()) throw BadRequestException("Editorial: no puede estar vacío")
        if (editorial.length > 100) throw BadRequestException("Editorial: no puede superar los 100 caracteres")
        if (publicationDate > LocalDate.now()) throw BadRequestException("Fecha de Publicación: no puede ser futura")
    }

    fun isAvailable(startDate: LocalDate, endDate: LocalDate) : Boolean {
        return reservationDates.none { it.from <= endDate && it.to >= startDate }
    }

    fun addReservation(reservation : Reservation) {
        reservationDates.add(DateRange(reservation.startDate, reservation.endDate))
        reservationsCount ++
    }

    fun getFutureReservations(): List<DateRange> {
        return reservationDates.filter { it.from >= LocalDate.now() }
    }

    fun getUserIdPostgres() = this.userPublisher.idPostgres

    abstract fun extraKarma(user: User): Int
    abstract fun frontName(): String
}

class CommonBook(
    title: String,
    description: String,
    coverUrl: String,
    author: String,
    pages: Int,
    isbn: String,
    language: BOOK_LANGUAGE,
    editorial: String,
    publicationDate: LocalDate,
    state: BOOK_STATE,
    genre: BOOK_GENRE
) : Book(title, description, coverUrl, author, pages, isbn, language,  editorial, publicationDate, state, genre) {
    override fun extraKarma(user: User): Int {
        val multiplier = if (user.bibliokarma < 1000) 5 else 2
        return multiplier * pages
    }
    override fun frontName(): String = "Común"
}

class DedicationBook(
    title: String,
    description: String,
    coverUrl: String,
    author: String,
    pages: Int,
    isbn: String,
    language: BOOK_LANGUAGE,
    editorial: String,
    publicationDate: LocalDate,
    state: BOOK_STATE,
    genre: BOOK_GENRE
) : Book(title, description, coverUrl, author, pages, isbn, language,  editorial, publicationDate, state, genre) {
    override fun extraKarma(user: User): Int = 200 + 10 * reservationsCount
    override fun frontName(): String = "Con dedicatoria"
}

class CollectibleBook(
    title: String,
    description: String,
    coverUrl: String,
    author: String,
    pages: Int,
    isbn: String,
    language: BOOK_LANGUAGE,
    editorial: String,
    publicationDate: LocalDate,
    state: BOOK_STATE,
    genre: BOOK_GENRE
) : Book(title, description, coverUrl, author, pages, isbn, language,  editorial, publicationDate, state, genre) {
    override fun extraKarma(user: User): Int = ceil(user.bibliokarma / 5.0).toInt() + pages
    override fun frontName(): String = "Coleccionable"
}

data class UserPublisher(var idPostgres: Int, var name: String)
data class DateRange(val from: LocalDate, val to: LocalDate)