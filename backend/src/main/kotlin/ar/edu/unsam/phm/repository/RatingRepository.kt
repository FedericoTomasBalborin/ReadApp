package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Rating
import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RatingRepository : JpaRepository<Rating, Int> {
    interface RatingStatsProjection {
        fun getAverage(): Double
        fun getCount(): Int
    }

    fun findByUserIdAndBookId(userId: Int, bookId: String): Rating?

    @EntityGraph(attributePaths = ["user"])
    fun findByBookId(bookId: String, limit: Limit): List<Rating>

    @Query(
    """
        SELECT
            COALESCE(AVG(r.calification), 0.0) AS average,
            COUNT(*) AS count
        FROM ratings r
        WHERE r.book_id = :bookId
    """, nativeQuery = true
    )
    fun getRatingAverageByBookId(bookId: String): RatingStatsProjection
}