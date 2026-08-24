package ar.edu.unsam.phm.controllers

import ar.edu.unsam.phm.dto.RatingRequestDTO
import ar.edu.unsam.phm.dto.RatingResponseDTO
import ar.edu.unsam.phm.services.RatingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.access.prepost.PreAuthorize

@RestController
@RequestMapping("/api/rating")
class RatingController (
    private val ratingService: RatingService
){
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('Lector')")
    fun createRating(auth: Authentication, @RequestBody ratingReqDTO: RatingRequestDTO) : ResponseEntity<Void>{
        val (idUser, _) = auth.principal as Pair<Int, String>
        ratingService.createRating(idUser, ratingReqDTO)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/getRatingByBook/{idBook}")
    @PreAuthorize("hasAnyAuthority('Lector','Publicador')")
    fun getBookRatings(@PathVariable idBook: String) : ResponseEntity<List<RatingResponseDTO>> {
        return ResponseEntity.ok(ratingService.getBookRatings(idBook))
    }
}