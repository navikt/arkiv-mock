package no.nav.soknad.archiving.joarkmock.service

import no.nav.soknad.archiving.dto.JoarkData
import no.nav.soknad.archiving.joarkmock.dto.JoarkDbData
import no.nav.soknad.archiving.joarkmock.repository.JoarkRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class JoarkMockService(private val joarkRepository: JoarkRepository) {

	fun archive(joarkData: JoarkData) {
		val data = createJoarkDbData(joarkData)
		joarkRepository.save(data)
	}

	fun lookup(name: String): List<JoarkDbData> {
		return joarkRepository.findByName(name)
	}

	private fun createJoarkDbData(joarkData: JoarkData) =
		JoarkDbData(UUID.randomUUID().toString(), joarkData.tema, joarkData.eksternReferanseId)
}
