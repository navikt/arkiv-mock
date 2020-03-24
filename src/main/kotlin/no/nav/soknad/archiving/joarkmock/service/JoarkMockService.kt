package no.nav.soknad.archiving.joarkmock.service

import no.nav.soknad.archiving.dto.Dokumenter
import no.nav.soknad.archiving.dto.JoarkData
import no.nav.soknad.archiving.dto.JoarkResponse
import no.nav.soknad.archiving.joarkmock.dto.JoarkDbData
import no.nav.soknad.archiving.joarkmock.repository.JoarkRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class JoarkMockService(private val joarkRepository: JoarkRepository, private val behaviourService: BehaviourService) {

	fun archive(joarkData: JoarkData): String {
		behaviourService.reactToArchiveRequest()

		val data = createJoarkDbData(joarkData)
		joarkRepository.save(data)

		val response = createResponse(joarkData, data)
		return behaviourService.alterResponse(response)
	}

	private fun createResponse(joarkData: JoarkData, data: JoarkDbData): JoarkResponse {
		val dokumenter = joarkData.dokumenter.map { Dokumenter(it.brevkode, UUID.randomUUID().toString(), it.tittel) }
		return JoarkResponse(dokumenter, data.id, true, "MIDLERTIDIG", "null")
	}

	fun lookup(name: String): List<JoarkDbData> {
		return joarkRepository.findByName(name)
	}

	private fun createJoarkDbData(joarkData: JoarkData) =
		JoarkDbData(UUID.randomUUID().toString(), joarkData.tema, joarkData.eksternReferanseId)
}
