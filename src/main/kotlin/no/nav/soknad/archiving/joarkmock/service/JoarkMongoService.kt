package no.nav.soknad.archiving.joarkmock.service

import no.nav.soknad.archiving.joarkmock.dto.JoarkData
import no.nav.soknad.archiving.joarkmock.repository.JoarkRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class JoarkMongoService(private val joarkRepository: JoarkRepository) {

	fun archive(id: String, message: String) {
		joarkRepository.save(JoarkData(UUID.randomUUID().toString(), message, id))
	}

	fun lookup(name: String): List<JoarkData> {
		return joarkRepository.findByName(name)
	}
}
