package no.nav.soknad.archiving.joarkmock.service

import no.nav.soknad.archiving.joarkmock.dto.JoarkDbData
import no.nav.soknad.archiving.joarkmock.repository.JoarkRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class JoarkMongoService(private val joarkRepository: JoarkRepository) {

	fun archive(id: String, message: String) {
		joarkRepository.save(JoarkDbData(UUID.randomUUID().toString(), message, id))
	}

	fun lookup(name: String): List<JoarkDbData> {
		return joarkRepository.findByName(name)
	}
}
