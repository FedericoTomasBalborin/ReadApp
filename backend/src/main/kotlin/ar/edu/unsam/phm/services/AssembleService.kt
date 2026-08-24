package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.dto.BookUpdatableFieldsDTO
import ar.edu.unsam.phm.dto.user.EditUserDTO
import ar.edu.unsam.phm.dto.user.UserDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

//Un service por un nivel encima de Book, User y Reservation cuyo proposito es comunicar a los service cuando estos necesitan datos menores de otros
// Pero que podrían causar referencias circulares

@Service
class AssembleService(
    private val bookService: BookService,
    private val userService: UserService,
    private val reservationService: ReservationService
) {
    fun updateUser(userId: Int, editUserDTO: EditUserDTO) {
        val user = userService.updateUser(userId, editUserDTO)
        bookService.updatePublisherData(user)
    }

    fun updateBook(idBook: String, data: BookUpdatableFieldsDTO, userId: Int) {
        val book = bookService.updateBook(idBook, data, userId)
        reservationService.updateBookData(book)
    }

    @Transactional(readOnly = true)
    fun getUserData(userId: Int): UserDTO {
        val user = userService.findById(userId)
        val countSharedBooks = reservationService.countReservationsByIdPublisher(userId)
        val countReadBooks = reservationService.countByUserIdAndEndDateBefore(userId, LocalDate.now())
        return UserDTO.createFrom(user, countSharedBooks, countReadBooks)
    }
}