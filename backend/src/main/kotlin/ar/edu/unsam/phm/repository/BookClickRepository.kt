package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.BookClick
import org.springframework.data.mongodb.repository.MongoRepository

interface BookClickRepository: MongoRepository<BookClick, String> {
    fun findByIdBookIn(bookIds: List<String>): List<BookClick>
}
