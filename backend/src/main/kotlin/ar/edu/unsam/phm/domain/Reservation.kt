package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.exceptions.BadRequestException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "reservations")
class Reservation(
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,
    val startDate: LocalDate,
    val endDate: LocalDate,
    @Transient
    val book : Book,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var isActive: Boolean = true
    var bibliokarmaLog: Int = 0
    val idBook: String = book.id
    val bookIdPublisher = book.userPublisher.idPostgres
    val bookTitle = book.title
    val bookAuthor = book.author
    val bookCoverUrl = book.coverUrl

    var createdAt: Instant = Instant.now()

    init {
        bibliokarmaLog = getKarma(book)
    }

    private fun getKarma(book: Book): Int = 5 * days() + book.extraKarma(user)

    fun validate() {
        val today = LocalDate.now()
        if(startDate < today) throw BadRequestException("La fecha no puede ser previa al dia actual")
        if(endDate < today) throw BadRequestException("La fecha no puede ser previa al dia actual")
        if(startDate > endDate) throw BadRequestException("La fecha de devolucion no puede ser anterior a la fecha de pedido")
    }

    fun days(): Int =
        ChronoUnit.DAYS.between(startDate, endDate).toInt()

    fun isOverlapping(newReservationStart: LocalDate, newReservationEnd: LocalDate): Boolean {
        return newReservationStart <= endDate && newReservationEnd >= startDate
    }

    fun getState(): String {
        val today = LocalDate.now()
        val daysUntilEnd = ChronoUnit.DAYS.between(today, endDate)

        return when {
            daysUntilEnd > 2 -> RESERVATION_STATE.ACTIVE.frontName
            daysUntilEnd >= 0 -> RESERVATION_STATE.NEAR_EXPIRATION.frontName
            else -> RESERVATION_STATE.RETURNED.frontName
        }
    }
}