package ar.edu.unsam.phm.controllers

import ar.edu.unsam.phm.dto.BibliokarmaDTO
import ar.edu.unsam.phm.dto.ReservationRequestDTO
import ar.edu.unsam.phm.dto.ReservationsPageDTO
import ar.edu.unsam.phm.services.ReservationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.access.prepost.PreAuthorize
import java.time.LocalDate

@RestController
@RequestMapping("/api/reservation")
class ReservationController(
    private val reservationService: ReservationService
) {
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('Lector')")
    fun createReservation(
        auth: Authentication,
        @RequestBody reservationReqDTO: ReservationRequestDTO
    ): ResponseEntity<Void> {
        val (idUser, _) = auth.principal as Pair<Int, String>
        reservationService.createReservation(
            idUser,
            reservationReqDTO)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/bibliokarma")
    @PreAuthorize("hasAuthority('Lector')")
    fun getBibliokarma(
        auth: Authentication,
        @RequestParam idBook: String,
        @RequestParam reservationStartDate: LocalDate,
        @RequestParam reservationEndDate: LocalDate
    ): ResponseEntity<BibliokarmaDTO> {
        val (idUser, _) = auth.principal as Pair<Int, String>
        return ResponseEntity.ok(
            reservationService.getBibliokarma(
                idUser,
                idBook,
                reservationStartDate,
                reservationEndDate
            )
        )
    }

    @GetMapping("/ownedBooksReservations")
    @PreAuthorize("hasAuthority('Publicador')")
    fun getOwnedBooksReservations(
        auth: Authentication,
        @RequestParam page: Int,
        @RequestParam size: Int,
        @RequestParam filter: String
    ): ResponseEntity<ReservationsPageDTO> {
        val (idUser, _) = auth.principal as Pair<Int, String>
        return ResponseEntity.ok(reservationService.getOwnedBooksReservations(idUser, page, size, filter))
    }

    @GetMapping("/myReservations")
    @PreAuthorize("hasAuthority('Lector')")
    fun getMyReservations(
        auth: Authentication,
        @RequestParam page: Int,
        @RequestParam size: Int,
        @RequestParam filter: String
    ): ResponseEntity<ReservationsPageDTO> {
        val (idUser, _) = auth.principal as Pair<Int, String>
        return ResponseEntity.ok(reservationService.getMyReservations(idUser, page, size, filter))
    }
}