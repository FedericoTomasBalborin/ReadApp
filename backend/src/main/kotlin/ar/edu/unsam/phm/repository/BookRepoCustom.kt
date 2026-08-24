package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.BOOK_GENRE
import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.dto.BOOKFILTER
import ar.edu.unsam.phm.dto.metrics.RatingByBookTypeDTO
import ar.edu.unsam.phm.dto.metrics.RatingByBookTypeMongo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOptions
import org.springframework.data.mongodb.core.query.Collation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.regex.Pattern

interface BookRepositoryCustom {
    fun findFilteredBooks(
        title: String?,
        isbn: String?,
        genres: List<BOOK_GENRE>?,
        minPages: Int?,
        maxPages: Int?,
        username: String?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        pageable: Pageable
    ): Page<Book>

    fun findBooksByPublisherAndFilter(
        publisherId: Int, filter: String
    ): List<Book>

    fun findBooksByIdAndFilter(
        booksIds: List<String>, filter: String, pageable: Pageable
    ): Page<Book>

    fun findMyBooks(
        publisherId: Int, filter: BOOKFILTER, startDate: LocalDate, endDate: LocalDate, pageable: Pageable
    ): Page<Book>

    fun cleanExpiredReservationDates(): Int

    fun findAllBooksWithRating(): List<RatingByBookTypeMongo>
}

@Repository
class BookRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : BookRepositoryCustom {

    override fun findAllBooksWithRating(): List<RatingByBookTypeMongo> {

        val aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("ratingCount").gt(0)),
            Aggregation.group("_class").avg("calification").`as`("averageRating"),
            Aggregation.project("averageRating").and("_id").`as`("type")
        )

        return mongoTemplate.aggregate(aggregation, "libros", RatingByBookTypeMongo::class.java).mappedResults
    }

    fun criteriaByTitleAndAuthor(filter: String): Criteria {
        var criteria = Criteria()

        filter.takeIf { it.isNotBlank() }?.let {
            val escapedFilter = Pattern.quote(it)

            criteria = Criteria().orOperator(
                Criteria.where("title").regex(".*$escapedFilter.*", "i"),
                Criteria.where("author").regex(".*$escapedFilter.*", "i")
            )
        }
        return criteria
    }

    fun pageImplementation(criteriaList: List<Criteria>, pageable: Pageable): Page<Book> {
        val finalCriteria = Criteria().andOperator(*criteriaList.toTypedArray())

        val sort = pageable.sort.and(Sort.by(Sort.Direction.ASC, "_id"))

        val aggregation = Aggregation.newAggregation(
            Aggregation.match(finalCriteria),
            Aggregation.sort(sort),
            Aggregation.skip(pageable.offset),
            Aggregation.limit(pageable.pageSize.toLong())
        )

        val options = AggregationOptions.builder()
            .collation(
                Collation.of("en")
                    .strength(Collation.ComparisonLevel.secondary())
            )
            .build()

        val books = mongoTemplate.aggregate<Book>(
            aggregation.withOptions(options), "libros"
        ).mappedResults

        val countAggregation = Aggregation.newAggregation(
            Aggregation.match(finalCriteria), Aggregation.count().`as`("total")
        )

        val countResult = mongoTemplate.aggregate<CountResult>(
            countAggregation, "libros"
        ).uniqueMappedResult

        val total = countResult?.total ?: 0L

        return PageImpl(
            books, pageable, total
        )
    }

    override fun findBooksByPublisherAndFilter(
        publisherId: Int, filter: String
    ): List<Book> {

        val criteriaList = mutableListOf<Criteria>()

        criteriaList.add(Criteria.where("userPublisher.idPostgres").`is`(publisherId))

        criteriaList.add(criteriaByTitleAndAuthor(filter))

        val finalCriteria = Criteria().andOperator(*criteriaList.toTypedArray())
        val query = Query(finalCriteria)

        return mongoTemplate.find(query, Book::class.java, "libros")
    }

    override fun findBooksByIdAndFilter(
        booksIds: List<String>, filter: String, pageable: Pageable
    ): Page<Book> {

        val criteriaList = mutableListOf<Criteria>()

        criteriaList.add(Criteria.where("_id").`in`(booksIds))

        criteriaList.add(criteriaByTitleAndAuthor(filter))

        return pageImplementation(criteriaList, pageable)
    }

    override fun findFilteredBooks(
        title: String?,
        isbn: String?,
        genres: List<BOOK_GENRE>?,
        minPages: Int?,
        maxPages: Int?,
        username: String?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        pageable: Pageable
    ): Page<Book> {

        val criteriaList = mutableListOf<Criteria>()

        criteriaList.add(
            Criteria.where("isActive").`is`(true)
        )

        title?.takeIf { it.isNotBlank() }?.let {
            criteriaList.add(
                Criteria.where("title").regex(".*$it.*", "i")
            )
        }

        isbn?.takeIf { it.isNotBlank() }?.let {
            criteriaList.add(
                Criteria.where("isbn").`is`(it)
            )
        }

        genres?.takeIf { it.isNotEmpty() }?.let {
            criteriaList.add(
                Criteria.where("genre").`in`(it)
            )
        }

        if (minPages != null || maxPages != null) {
            val pagesCriteria = Criteria.where("pages")
            minPages?.let {
                pagesCriteria.gte(it)
            }
            maxPages?.let {
                pagesCriteria.lte(it)
            }
            criteriaList.add(pagesCriteria)
        }

        username?.takeIf { it.isNotBlank() }?.let {
            criteriaList.add(
                Criteria.where("userPublisher.name").regex(".*$it.*", "i")
            )
        }

        if (fromDate != null && toDate != null) {
            criteriaList.add(
                Criteria.where("reservationDates").not().elemMatch(
                    Criteria().andOperator(
                        Criteria.where("from").lte(toDate), Criteria.where("to").gte(fromDate)
                    )
                )
            )
        }

        return pageImplementation(criteriaList, pageable)
    }

    override fun findMyBooks(
        publisherId: Int, filter: BOOKFILTER, startDate: LocalDate, endDate: LocalDate, pageable: Pageable
    ): Page<Book> {
        val criteriaList = mutableListOf(
            Criteria.where("userPublisher.idPostgres").`is`(publisherId)
        )

        val overlapCriteria = Criteria().andOperator(
            Criteria.where("from").lte(endDate),
            Criteria.where("to").gte(startDate)
        )

        when (filter) {
            BOOKFILTER.ALL -> {}

            BOOKFILTER.AVAILABLE -> {
                criteriaList.add(Criteria.where("isActive").`is`(true))
                criteriaList.add(Criteria.where("reservationDates").not().elemMatch(overlapCriteria))
            }

            BOOKFILTER.BORROWED -> {
                criteriaList.add(Criteria.where("isActive").`is`(true))
                criteriaList.add(Criteria.where("reservationDates").elemMatch(overlapCriteria))
            }

            BOOKFILTER.DELETED -> criteriaList.add(Criteria.where("isActive").`is`(false))
        }

        return pageImplementation(criteriaList, pageable)
    }

    override fun cleanExpiredReservationDates(): Int {
        val today = LocalDate.now()

        val criteria = Criteria.where("reservationDates.to").lt(today)

        val update = Update().pull("reservationDates", Criteria.where("to").lt(today))

        val result = mongoTemplate.updateMulti(
            Query(criteria),
            update,
            "libros"
        )

        return result.modifiedCount.toInt()
    }

    data class CountResult(
        val total: Long = 0
    )
}