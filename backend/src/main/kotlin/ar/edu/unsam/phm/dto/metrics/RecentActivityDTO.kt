package ar.edu.unsam.phm.dto.metrics

import ar.edu.unsam.phm.domain.User
import java.time.Instant


data class RecentActivityDTO(
    val date: Instant,
    val eventType: EVENT_TYPE,
    val user: User
)

enum class EVENT_TYPE {
    BOOK_CREATED,
    RESERVATION_CREATED,
}