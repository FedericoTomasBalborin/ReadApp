package ar.edu.unsam.phm.dto.metrics

data class BookConversionsDTO(
    val idBook: String,
    val clickCount: Int,
    val reservationCount: Int,
    val conversionRate: Float
)