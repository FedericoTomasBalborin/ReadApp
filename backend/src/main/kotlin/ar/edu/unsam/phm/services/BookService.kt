package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.BOOK_GENRE
import ar.edu.unsam.phm.domain.BOOK_LANGUAGE
import ar.edu.unsam.phm.domain.BOOK_STATE
import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.BookClick
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserPublisher
import ar.edu.unsam.phm.dto.BOOKFILTER
import ar.edu.unsam.phm.dto.BookCardDTO
import ar.edu.unsam.phm.dto.BookRowDTO
import org.springframework.stereotype.Service
import ar.edu.unsam.phm.dto.FilteredBooksDTO
import ar.edu.unsam.phm.dto.BookDetailDTO
import ar.edu.unsam.phm.dto.BookFiltersDTO
import ar.edu.unsam.phm.dto.BookUpdatableFieldsDTO
import ar.edu.unsam.phm.dto.FormFieldsDTO
import ar.edu.unsam.phm.dto.MyBooksResponse
import ar.edu.unsam.phm.dto.ORDER
import ar.edu.unsam.phm.dto.ReservationDatesDTO
import ar.edu.unsam.phm.dto.metrics.RatingByBookTypeMongo
import ar.edu.unsam.phm.exceptions.ForbiddenException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import ar.edu.unsam.phm.exceptions.NotFoundException
import ar.edu.unsam.phm.repository.BookClickRepository
import ar.edu.unsam.phm.repository.BookRepository

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val bookClickRepository: BookClickRepository,
    private val userService: UserService,
    private val cachedBookService: CachedBookService
) {

    @Transactional(readOnly = true)
    fun getFilteredBooks(filters: BookFiltersDTO, page: Int, size: Int): FilteredBooksDTO {

        cachedBookService.getCachedTopBooks(filters)?.let {
            return FilteredBooksDTO(
                books = it,
                totalFilteredBooks = 0
            )
        }

        val sort = Sort.by(
            Sort.Direction.ASC,
            filters.order?.column
        )

        val pageable = PageRequest.of(page - 1, size, sort)
        val filteredResult = bookRepository.findFilteredBooks(
            title = filters.title?.trim()?.lowercase(),
            isbn = filters.isbn,
            genres = filters.genres,
            maxPages = filters.maxPages,
            minPages = filters.minPages,
            username = filters.username?.trim()?.lowercase(),
            fromDate = filters.from,
            toDate = filters.to,
            pageable = pageable
        )

        val booksDTO = filteredResult.content.map { BookCardDTO.createFrom(it, it.userPublisher.name) }

        cachedBookService.cachedBooks(booksDTO)

        return FilteredBooksDTO(
            books = booksDTO,
            totalFilteredBooks = filteredResult.totalElements
        )
    }

    @Transactional(readOnly = true)
    fun getMyBooks(userId: Int, page: Int, filter: BOOKFILTER, order: ORDER, isASC: Boolean): MyBooksResponse {
        val today = LocalDate.now()
        val direction = if (isASC) Sort.Direction.ASC else Sort.Direction.DESC

        val sort = Sort.by(direction, order.column)
        val pageableWithSort = PageRequest.of(page - 1, 3, sort)

        val books =
            bookRepository.findMyBooks(userId, filter, today, today, pageableWithSort)

        val bookClicks = bookClickRepository.findByIdBookIn(books.content.map { it.id })

        val bookRows = books.content.map { book ->
            BookRowDTO.createFrom(
                book,
                isAvailable = book.isAvailable(today, today),
                clickCount = bookClicks.count { click -> click.idBook == book.id }
            )
        }

        return MyBooksResponse(
            books = bookRows,
            totalSize = books.totalElements
        )
    }

    @Transactional(readOnly = true)
    fun getBookDetailById(idBook: String, idUser: Int, startDate: LocalDate, endDate: LocalDate) : BookDetailDTO {
        val user = userService.findById(idUser)
        val book = findById(idBook)
        val reservation = Reservation(user, startDate, endDate, book)
        val bibliokarma = reservation.bibliokarmaLog
        val ratingAverage = book.calification
        val ratingCount = book.ratingCount
        val firstTwoRatings = book.firstTwoRatings
        val reservationDates = book.getFutureReservations().map { ReservationDatesDTO.createFrom(it) }
        if (user.id != book.getUserIdPostgres()) {
            bookClickRepository.save(BookClick(idBook, user.name))
            cachedBookService.registerClick(book.id)
        }
        return BookDetailDTO.buildBookDetailDTO(book, reservationDates, ratingAverage, ratingCount, firstTwoRatings, bibliokarma)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun deleteBookById(idBook: String, userId: Int) {
        val book = findById(idBook)
        validatePublisher(userId, book)
        book.isActive = false
        bookRepository.save(book)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun restoreBookById(idBook: String, userId: Int) {
        val book = findById(idBook)
        validatePublisher(userId, book)
        book.isActive = true
        bookRepository.save(book)
    }

    @Transactional(readOnly = true)
    fun fillForm(idBook: String): FormFieldsDTO {
        val book = findById(idBook)
        return FormFieldsDTO.from(book)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createBook(userId: Int, book: Book) {
        val publisher = userService.findById(userId)
        book.userPublisher = UserPublisher(
            publisher.id!!,
            publisher.name
        )
        book.validate()
        bookRepository.save(book)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateBook(idBook: String, data: BookUpdatableFieldsDTO, userId: Int) : Book {
        val book = findById(idBook)
        validatePublisher(userId, book)
        update(book, data)
        book.validate()
        return bookRepository.save(book)
    }

    private fun validatePublisher(userId: Int, book: Book) {
        if (userId != book.userPublisher.idPostgres) throw ForbiddenException("El usuario con id $userId no es el publicador del libro")
    }

    private fun update(book: Book, data: BookUpdatableFieldsDTO) {
        book.title = data.title
        book.description = data.description
        book.coverUrl = data.coverUrl
        book.author = data.author
        book.pages = data.pages
        book.isbn = data.isbn
        book.editorial = data.editorial
        book.publicationDate = data.publicationDate
        book.language = BOOK_LANGUAGE.fromFrontName(data.language)
        book.state = BOOK_STATE.fromFrontName(data.state)
        book.genre = BOOK_GENRE.fromFrontName(data.genre)
    }

    fun findById(id: String): Book {
        return bookRepository.findById(id).orElseThrow { NotFoundException("El libro con ${id} no existe") }
    }

    fun saveBook(book: Book) {
        bookRepository.save(book)
    }

    fun updatePublisherData(publisher : User) {
        bookRepository.updatePublisherNameByPublisherId(publisher.id!!, publisher.name)
    }

    fun findAllBooksWithRating(): List<RatingByBookTypeMongo> {
        return bookRepository.findAllBooksWithRating()
    }

    fun findTop5ByOrderByCreatedAtDesc(): List<Book> {
        return bookRepository.findTop5ByOrderByCreatedAtDesc()
    }

    fun countBooks(): Long {
        return bookRepository.count()
    }
}