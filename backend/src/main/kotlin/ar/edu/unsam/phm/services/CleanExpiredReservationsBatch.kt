package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.repository.BookRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class CleanExpiredReservationsBatch(
    private val bookRepository: BookRepository
) {
    private val logger = LoggerFactory.getLogger(CleanExpiredReservationsBatch::class.java)

    @Scheduled(cron = "0 0 0 * * ?")
    fun cleanExpiredReservations() {
        logger.info("Iniciando limpieza de reservas vencidas...")

        try {
            val today = LocalDate.now()

            val cleanedCount = bookRepository.cleanExpiredReservationDates()
            logger.info("Limpieza completada: se eliminaron $cleanedCount DateRange vencidos")

            val allBooks = bookRepository.findAll()

            allBooks.forEach { book ->
                val hasActiveReservationToday = book.reservationDates.any { dateRange ->
                    dateRange.from <= today && dateRange.to >= today
                }
                book.currentlyBorrowed = hasActiveReservationToday
            }

            bookRepository.saveAll(allBooks)
            logger.info("Estado actualizado para ${allBooks.size} libros")

        } catch (e: Exception) {
            logger.error("Error durante la limpieza de reservas vencidas", e)
        }
    }
}