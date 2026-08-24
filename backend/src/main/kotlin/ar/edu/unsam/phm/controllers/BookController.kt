package ar.edu.unsam.phm.controllers

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.dto.BOOKFILTER
import ar.edu.unsam.phm.dto.BookCardDTO
import ar.edu.unsam.phm.dto.FilteredBooksDTO
import ar.edu.unsam.phm.dto.BookDetailDTO
import ar.edu.unsam.phm.dto.BookFiltersDTO
import ar.edu.unsam.phm.dto.BookUpdatableFieldsDTO
import ar.edu.unsam.phm.dto.FormFieldsDTO
import ar.edu.unsam.phm.dto.MyBooksResponse
import ar.edu.unsam.phm.dto.ORDER
import ar.edu.unsam.phm.services.AssembleService
import ar.edu.unsam.phm.services.BookService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.access.prepost.PreAuthorize
import java.time.LocalDate

@RestController
@RequestMapping("/books")
class BookController(
    private val bookService: BookService,
    private val assembleService: AssembleService
) {

    @PostMapping("/filtered")
    @PreAuthorize("hasAnyAuthority('Lector','Publicador')")
    fun getFilteredBooks(
        @RequestParam page: Int,
        @RequestParam size: Int,
        @RequestBody filters: BookFiltersDTO
    ): FilteredBooksDTO {
        return bookService.getFilteredBooks(filters, page, size)
    }

    @GetMapping("/mybooks") //Obtiene todos los libros que correspondan a su ID, por conveniencia, se retornaran de a segmentos
    @PreAuthorize("hasAuthority('Publicador')")
    fun getMyBooks(
        auth: Authentication,
        @RequestParam page: Int,
        @RequestParam filterBy: BOOKFILTER,
        @RequestParam orderBy: ORDER,
        @RequestParam isASC: Boolean,
    ): ResponseEntity<MyBooksResponse> {
        val (userId, _) = auth.principal as Pair<Int, String>
        return ResponseEntity.ok(bookService.getMyBooks(userId, page, filterBy, orderBy, isASC))
    }

    @GetMapping("/book-detail/{idBook}")
    @PreAuthorize("hasAnyAuthority('Lector','Publicador')")
    fun getBookById(
        auth: Authentication,
        @PathVariable idBook: String,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate
    ): ResponseEntity<BookDetailDTO> {
        val (idUser, _) = auth.principal as Pair<Int, String>
        return ResponseEntity.ok(bookService.getBookDetailById(idBook, idUser, startDate, endDate))
    }

    @DeleteMapping("/{idBook}")
    @PreAuthorize("hasAuthority('Publicador')")
    fun deleteBookById(auth: Authentication, @PathVariable idBook: String): ResponseEntity<Void> {
        val (userId, _) = auth.principal as Pair<Int, String>
        bookService.deleteBookById(idBook, userId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{idBook}/restore")
    @PreAuthorize("hasAuthority('Publicador')")
    fun restoreBookById(auth: Authentication, @PathVariable idBook: String): ResponseEntity<Void> {
        val (userId, _) = auth.principal as Pair<Int, String>
        bookService.restoreBookById(idBook, userId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/form/{idBook}")
    @PreAuthorize("hasAuthority('Publicador')")
    fun fillForm(@PathVariable idBook: String): ResponseEntity<FormFieldsDTO> {
        return ResponseEntity.ok(bookService.fillForm(idBook))
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('Publicador')")
    fun createBook(auth: Authentication, @RequestBody book: Book): ResponseEntity<Void> {
        val (userId, _) = auth.principal as Pair<Int, String>
        bookService.createBook(userId, book)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PutMapping("/update/{idBook}")
    @PreAuthorize("hasAuthority('Publicador')")
    fun updateBook(
        auth: Authentication,
        @PathVariable idBook: String,
        @RequestBody data: BookUpdatableFieldsDTO
    ): ResponseEntity<Void> {
        val (userId, _) = auth.principal as Pair<Int, String>
        assembleService.updateBook(idBook, data, userId)
        return ResponseEntity.noContent().build()
    }
}