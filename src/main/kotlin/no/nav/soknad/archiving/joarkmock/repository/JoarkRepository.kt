package no.nav.soknad.archiving.joarkmock.repository

import no.nav.soknad.archiving.joarkmock.dto.JoarkData
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface JoarkRepository: MongoRepository<JoarkData, String> {
	fun findByName(name: String): List<JoarkData>
}
