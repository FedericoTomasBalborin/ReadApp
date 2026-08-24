package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.DateRange
import ar.edu.unsam.phm.domain.Reservation
import java.time.LocalDate

data class ReservationRequestDTO(
    val idUser: Int,
    val idBook: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class ReservationDTO(
    val idBook: String,
    val coverBook: String,
    val state: String,
    val title: String,
    val author: String,
    val publisher: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val bibliokarma: Int,
) {
    companion object {
        fun createFrom(reservation: Reservation, publisherName: String): ReservationDTO {
            return ReservationDTO(
                idBook = reservation.idBook,
                coverBook = reservation.bookCoverUrl,
                state = reservation.getState(),
                title = reservation.bookTitle,
                author = reservation.bookAuthor,
                publisher = publisherName,
                startDate = reservation.startDate,
                endDate = reservation.endDate,
                bibliokarma = reservation.bibliokarmaLog
            )
        }
    }
}

data class ReservationDatesDTO(
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    companion object {
        fun createFrom(dateRange: DateRange): ReservationDatesDTO {
            return ReservationDatesDTO(
                startDate = dateRange.from,
                endDate = dateRange.to
            )
        }
    }
}

data class ReservationsPageDTO(val totalReservations: Long, val reservations: List<ReservationDTO>)