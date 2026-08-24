package ar.edu.unsam.phm.domain

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("CachedBooks", timeToLive = 5 * 60)
data class CachedBook (
    @Id
    val id: String,
    val title: String,
    val coverUrl: String,
    val author: String,
    val isbn: String,
    val language: BOOK_LANGUAGE,
    val state: BOOK_STATE,
    val genre: BOOK_GENRE,
    val calification: Double,
    val publisherName: String
)