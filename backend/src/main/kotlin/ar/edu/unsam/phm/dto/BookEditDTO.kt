package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Book
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.Length
import org.jetbrains.annotations.NotNull
import java.time.LocalDate

data class FormFieldsDTO(
    val bookType: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val author: String,
    val pages: Int,
    val isbn: String,
    val editorial: String,
    val publicationDate: LocalDate,
    val language: String,
    val state: String,
    val genre: String,
) {
    companion object {
        fun from(book: Book): FormFieldsDTO {
            return FormFieldsDTO(
                bookType = book.frontName(),
                title = book.title,
                description = book.description,
                coverUrl = book.coverUrl,
                author = book.author,
                pages = book.pages,
                isbn = book.isbn,
                genre = book.genre.frontName,
                editorial = book.editorial,
                language = book.language.frontName,
                publicationDate = book.publicationDate,
                state = book.state.frontName
            )
        }
    }
}

@JsonIgnoreProperties(value = ["bookType"])
data class BookUpdatableFieldsDTO(
    val title: String,
    val description: String,
    val coverUrl: String,
    val author: String,
    val pages: Int,
    val isbn: String,
    val editorial: String,
    val publicationDate: LocalDate,
    val language: String,
    val state: String,
    val genre: String
)