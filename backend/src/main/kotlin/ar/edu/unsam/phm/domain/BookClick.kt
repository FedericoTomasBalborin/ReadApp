package ar.edu.unsam.phm.domain

import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "book_clicks")
class BookClick(
    val idBook: String,
    val userClicked: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)