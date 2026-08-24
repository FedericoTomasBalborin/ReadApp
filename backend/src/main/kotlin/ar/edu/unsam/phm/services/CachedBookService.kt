package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.CachedBook
import ar.edu.unsam.phm.dto.BookCardDTO
import ar.edu.unsam.phm.dto.BookClicks
import ar.edu.unsam.phm.dto.BookFiltersDTO
import ar.edu.unsam.phm.repository.CachedBookRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class CachedBookService(
    private val cachedBookRepository: CachedBookRepository,
    private val redisTemplate: RedisTemplate<String, String>
){
    companion object {
        private const val RANKING_KEY = "books-ranking"
    }

    fun registerClick(bookId: String) {
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, bookId, 1.0)
    }

    fun getTop10Books(): List<CachedBook>? {
        val ids = redisTemplate.opsForZSet()
            .reverseRange(RANKING_KEY, 0, 9)
            ?.toList()
            ?: emptyList()

        val books = cachedBookRepository.findAllById(ids).toList()

        return books.takeIf{books.size >= 6 }
    }

    fun getTop5BookClicks(): List<BookClicks> {
        return redisTemplate.opsForZSet()
            .reverseRangeWithScores(RANKING_KEY, 0, 4)
            ?.map { tuple ->
                BookClicks(
                    bookId = tuple.value!!,
                    clicks = tuple.score!!.toInt()
                )
            }
            ?: emptyList()
    }

    fun cachedBooks(books: List<BookCardDTO>) {
        val cachedBooks = books.map { BookCardDTO.toCachedBook(it) }
        cachedBookRepository.saveAll(cachedBooks)
    }

    fun getCachedTopBooks(filters: BookFiltersDTO): List<BookCardDTO>? {
        if (filters.isFilter) return null

        return getTop10Books()?.map{
            bookCached -> BookCardDTO.createFrom(bookCached)
        }
    }
}