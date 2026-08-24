package ar.edu.unsam.phm.dto.metrics

data class CatalogHealthStatusDTO(
    val neverBeenReserved : Long,
    val reservedToday : Long,
    val availableWithFutureReservations : Long,
    val availableWithPastReservation : Long,
    val total : Long
)