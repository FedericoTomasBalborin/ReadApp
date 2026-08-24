package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.Update


interface BookRepository : MongoRepository<Book, String>, BookRepositoryCustom {
	@Query("{ 'userPublisher.idPostgres': ?0 }")
	@Update("{ '\$set': { 'userPublisher.name': ?1 } }")
	fun updatePublisherNameByPublisherId(publisherId: Int, publisherName: String): Long

	// Obtener los ultimos 5 libros que hayan sido creados
	fun findTop5ByOrderByCreatedAtDesc() : List<Book>
}