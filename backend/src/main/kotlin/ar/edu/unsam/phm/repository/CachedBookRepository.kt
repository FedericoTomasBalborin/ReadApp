package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.CachedBook
import org.springframework.data.repository.CrudRepository

interface CachedBookRepository : CrudRepository<CachedBook, String> {
}