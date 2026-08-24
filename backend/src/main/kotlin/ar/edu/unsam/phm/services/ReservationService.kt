package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Paginado
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.dto.BibliokarmaDTO
import ar.edu.unsam.phm.dto.BookClicks
import ar.edu.unsam.phm.dto.ReservationDTO
import ar.edu.unsam.phm.dto.ReservationsPageDTO
import ar.edu.unsam.phm.dto.ReservationRequestDTO
import ar.edu.unsam.phm.exceptions.BadRequestException
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.ReservationRepository.CountReservationState
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val bookService: BookService,
    private val userService: UserService
) {

    @Transactional(rollbackFor = [Exception::class])
    fun createReservation(idUser: Int, reservationReqDTO: ReservationRequestDTO) {
        val newReservationStart = reservationReqDTO.startDate
        val newReservationEnd = reservationReqDTO.endDate

        val book = bookService.findById(reservationReqDTO.idBook)

        if (!book.isAvailable(newReservationStart, newReservationEnd)) {
            throw BadRequestException("Ya hay una reserva en esas fechas")
        }

        val user = userService.findById(idUser)

        val reservation = Reservation(user, newReservationStart, newReservationEnd, book)
        reservation.validate()
        reservationRepository.save(reservation)

        book.addReservation(reservation)

        // ACTUALIZAR currentlyBorrowed
        val today = LocalDate.now()
        book.currentlyBorrowed = book.reservationDates.any { dateRange ->
            dateRange.from <= today && dateRange.to >= today
        }

        bookService.saveBook(book)
        user.earnBibliokarmaPoints(reservation.bibliokarmaLog)
    }

    @Transactional(readOnly = true)
    fun getBibliokarma(
        idUser: Int, idBook: String, reservationStartDate: LocalDate, reservationEndDate: LocalDate
    ): BibliokarmaDTO {
        val book = bookService.findById(idBook)
        val user = userService.findById(idUser)

        val reservation = Reservation(
            user = user, startDate = reservationStartDate, endDate = reservationEndDate, book = book
        )
        reservation.validate()

        val bibliokarma = reservation.bibliokarmaLog

        return BibliokarmaDTO(bibliokarma)
    }

    @Transactional(readOnly = true)
    fun getOwnedBooksReservations(idUser: Int, page: Int, size: Int, filter: String): ReservationsPageDTO {
        Paginado.validate(page, size)
        val normalizedFilter = filter.trim()
        val pageable = PageRequest.of(page - 1, size)

        val ownedBooksReservations = reservationRepository.findByPublisherAndFilter(idUser, normalizedFilter, pageable)

        val reservations = ownedBooksReservations.content.map { ReservationDTO.createFrom(it.getReservation(), it.getPublisherName()) }

        return ReservationsPageDTO(
            ownedBooksReservations.totalElements, reservations
        )
    }

    @Transactional(readOnly = true)
    fun getMyReservations(idUser: Int, page: Int, size: Int, filter: String): ReservationsPageDTO {
        Paginado.validate(page, size)
        val pageable = PageRequest.of(page - 1, size)
        val normalizedFilter = filter.trim()

        val myReservations = reservationRepository.findByUserAndFilter(idUser, normalizedFilter, pageable)

        val reservations = myReservations.content.map { ReservationDTO.createFrom(it.getReservation(), it.getPublisherName()) }

        return ReservationsPageDTO(
            myReservations.totalElements, reservations
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateBookData(book : Book) {
        reservationRepository.updateReservation(book.id, book.title, book.author, book.coverUrl)
    }

    fun countReservationsByIdPublisher(userId: Int) : Int {
        return reservationRepository.countReservationsByIdPublisher(userId)
    }

    fun countByUserIdAndEndDateBefore(userId: Int, today: LocalDate) : Int {
        return reservationRepository.countByUserIdAndEndDateBefore(userId, today)
    }

    fun findTop5ByOrderByCreatedAtDesc(): List<Reservation> {
        return reservationRepository.findTop5ByOrderByCreatedAtDesc()
    }

    fun countReservationByBookId(top5Clicks: List<BookClicks>): Map<String, Int> {
        return reservationRepository.countReservationByBookId(top5Clicks.map { it.bookId })
            .associate { it.getBookId() to it.getResCount().toInt() }
    }

    fun countBooksByTheirReservationState(date: LocalDate): CountReservationState {
        return reservationRepository.countBooksByTheirReservationState(date)
    }
}