package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.dto.metrics.BookConversionsDTO
import ar.edu.unsam.phm.dto.metrics.BookType
import ar.edu.unsam.phm.dto.metrics.CatalogHealthStatusDTO
import ar.edu.unsam.phm.dto.metrics.EVENT_TYPE
import ar.edu.unsam.phm.dto.metrics.RatingByBookTypeDTO
import ar.edu.unsam.phm.dto.metrics.RecentActivityDTO
import ar.edu.unsam.phm.dto.metrics.UserKarmaDTO
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

@Service
class MetricService(
    private val bookService: BookService,
    private val cachedBookService: CachedBookService,
    private val reservationService: ReservationService,
    private val userService: UserService,
    private val redisTemplate: RedisTemplate<String, String>
) {

    fun ratingAnalysis(): List<RatingByBookTypeDTO> {
        val projection = bookService.findAllBooksWithRating()
        return projection.map { RatingByBookTypeDTO(BookType.fromFrontName(it.type), it.averageRating) }
    }

    fun usersKarmaTop5(): List<UserKarmaDTO> {
        val cachedUsers = redisTemplate.opsForZSet()
            .reverseRangeWithScores("users:karma", 0, 4)
            ?.map { user ->
                UserKarmaDTO(
                    username = user.value!!,
                    bibliokarma = user.score!!.toInt()
                )
            }
            ?: emptyList()

        if (!cachedUsers.isEmpty()) return cachedUsers

        val users =  userService.findTop5UsersHighestBibliokarma().map {
            UserKarmaDTO(
                username = "${it.getName()} ${it.getLastname()}",
                bibliokarma = it.getBibliokarma()
            )
        }
        users.forEach {
            redisTemplate
                .opsForZSet()
                .add("users:karma", it.username, it.bibliokarma.toDouble())
        }
        redisTemplate.expire("users:karma", Duration.ofHours(1))

        return users
    }

    fun conversionRate(): List<BookConversionsDTO> {
        val top5Clicks = cachedBookService.getTop5BookClicks()
        if (top5Clicks.isEmpty()) return emptyList()

        val reservationsByBookId = reservationService.countReservationByBookId(top5Clicks)

        return top5Clicks.map { bookClick ->
            val reservationCount = reservationsByBookId[bookClick.bookId] ?: 0
            val conversion = if (bookClick.clicks == 0) 0f else reservationCount.toFloat() / bookClick.clicks.toFloat()

            BookConversionsDTO(
                bookClick.bookId,
                bookClick.clicks,
                reservationCount,
                conversion
            )
        }
    }

    fun recentActivityFeed(): List<RecentActivityDTO> {
        val recentBookActivity = bookService.findTop5ByOrderByCreatedAtDesc()
        val recentReservationActivity = reservationService.findTop5ByOrderByCreatedAtDesc()

        val sortedByDate = (recentBookActivity.map {
            RecentActivity(
                date = it.createdAt,
                eventType = EVENT_TYPE.BOOK_CREATED,
                userId = it.userPublisher.idPostgres
            )
        } + recentReservationActivity.map {
            RecentActivity(
                date = it.createdAt,
                eventType = EVENT_TYPE.RESERVATION_CREATED,
                userId = it.user.id!!
            )
        })
            .sortedByDescending { it.date }
            .take(5)

        val userIds = sortedByDate.map { it.userId }.distinct()
        val users = userService.findAllByIdWithMapping(userIds)

        return sortedByDate.map {
            RecentActivityDTO(
                date = it.date,
                eventType = it.eventType,
                user = users[it.userId]!!
            )
        }
    }

    fun catalogHealthStatus(): CatalogHealthStatusDTO {
        val reservationCounts = reservationService.countBooksByTheirReservationState(LocalDate.now())
        val totalBookCount = bookService.countBooks()

        val totalBooksReserved = (reservationCounts.getReservedToday() + reservationCounts.getAvailableWithFutureReservations()  + reservationCounts.getAvailableWithPastReservations())

        return CatalogHealthStatusDTO(
            neverBeenReserved = totalBookCount - totalBooksReserved,
            reservedToday = reservationCounts.getReservedToday(),
            availableWithFutureReservations = reservationCounts.getAvailableWithFutureReservations(),
            availableWithPastReservation =  reservationCounts.getAvailableWithPastReservations(),
            total = totalBookCount,
        )
    }
}

//Esto está privado porque es una dataclass temporal para facilitar la creación del DTO
// porque libro persiste solo el user id mientras que Reservation puede traer el usuario completo.
private data class RecentActivity(
    val date: Instant,
    val eventType: EVENT_TYPE,
    val userId: Int,
)