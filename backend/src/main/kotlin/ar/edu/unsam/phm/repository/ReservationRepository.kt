package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Reservation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ReservationRepository : JpaRepository<Reservation, Int> {

    interface ReservationWithPublisherName {
        fun getReservation(): Reservation
        fun getPublisherName(): String
    }

    interface ReservationCountWithBookId {
        fun getBookId(): String
        fun getResCount(): Long
    }

    interface CountReservationState {
        fun getReservedToday(): Long
        fun getAvailableWithFutureReservations(): Long
        fun getAvailableWithPastReservations(): Long
    }


    @Query(
        nativeQuery = true,
        value = """
            SELECT
                COUNT(CASE WHEN has_current THEN 1 END) AS reserved_today,
                COUNT(CASE WHEN NOT has_current AND has_future THEN 1 END) AS available_with_future_reservations,
                COUNT(CASE WHEN NOT has_current AND NOT has_future THEN 1 END) AS available_with_past_reservations
            FROM (
                SELECT
                    id_book,
                    MAX(CASE
                            WHEN start_date <= '2026-06-11'
                             AND end_date >= '2026-06-11'
                            THEN 1 ELSE 0
                        END) = 1 AS has_current,
            
                    MAX(CASE
                            WHEN start_date > '2026-06-11'
                            THEN 1 ELSE 0
                        END) = 1 AS has_future
                FROM reservations
                GROUP BY id_book
            ) book_status;
        """
    )
    fun countBooksByTheirReservationState(today: LocalDate) : CountReservationState



    @Query("""
        select r.idBook as bookId, count(r) as resCount
        from Reservation r
        where r.idBook in :bookIds
        group by r.idBook
    """)
    fun countReservationByBookId(@Param("bookIds") bookIds: List<String>): List<ReservationCountWithBookId>

    @Query(
        value = """
            select r as reservation, u.name as publisherName
            from Reservation r
            inner join User u
            on r.bookIdPublisher = u.id 
            where r.bookIdPublisher = :publisherId
            and (
                :filter = ''
                or lower(r.bookTitle) like concat('%', lower(:filter), '%')
                or lower(r.bookAuthor) like concat('%', lower(:filter), '%')
            )
            order by r.id
        """,
        countQuery = """
            select count(r)
            from Reservation r
            where r.bookIdPublisher = :publisherId
            and (
                :filter = ''
                or lower(r.bookTitle) like concat('%', lower(:filter), '%')
                or lower(r.bookAuthor) like concat('%', lower(:filter), '%')
            )
        """
    )
    fun findByPublisherAndFilter(
        @Param("publisherId") publisherId: Int,
        @Param("filter") filter: String,
        pageable: Pageable
    ): Page<ReservationWithPublisherName>

    @Query(
        value = """
            select r as reservation, u.name as publisherName
            from Reservation r
            inner join User u
            on r.bookIdPublisher = u.id 
            where r.user.id = :userId
            and (
                :filter = ''
                or lower(r.bookTitle) like concat('%', lower(:filter), '%')
                or lower(r.bookAuthor) like concat('%', lower(:filter), '%')
            )
            order by r.id
        """,
        countQuery = """
            select count(r)
            from Reservation r
            where r.user.id = :userId
            and (
                :filter = ''
                or lower(r.bookTitle) like concat('%', lower(:filter), '%')
                or lower(r.bookAuthor) like concat('%', lower(:filter), '%')
            )
        """
    )
    fun findByUserAndFilter(
        @Param("userId") userId: Int,
        @Param("filter") filter: String,
        pageable: Pageable
    ): Page<ReservationWithPublisherName>

    @Query("""
        select count(r)
        from Reservation r
        where r.bookIdPublisher = :idPublisher
    """)
    fun countReservationsByIdPublisher(idPublisher: Int): Int

    @Modifying
    @Query("""
        update Reservation r
        SET
            r.bookTitle = :bookTitle,
            r.bookAuthor = :bookAuthor,
            r.bookCoverUrl = :bookCoverUrl
        where r.idBook = :bookId
    """)
    fun updateReservation(
        @Param("bookId") bookId: String,
        @Param("bookTitle") bookTitle: String,
        @Param("bookAuthor") bookAuthor: String,
        @Param("bookCoverUrl") bookCoverUrl: String,
    )

    fun countByUserIdAndEndDateBefore(userId: Int, today: LocalDate): Int

    @EntityGraph(attributePaths = ["user"])
    fun findTop5ByOrderByCreatedAtDesc(): List<Reservation>
}


