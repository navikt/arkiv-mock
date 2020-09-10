package no.nav.soknad.archiving.joarkmock.service

import no.nav.soknad.archiving.dto.Dokumenter
import no.nav.soknad.archiving.dto.JoarkData
import no.nav.soknad.archiving.dto.OpprettJournalpostResponse
import no.nav.soknad.archiving.joarkmock.dto.JoarkDbData
import no.nav.soknad.archiving.joarkmock.repository.JoarkRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class JoarkMockService(private val joarkRepository: JoarkRepository, private val behaviourService: BehaviourService) {

	fun archive(joarkData: JoarkData): String? {
		behaviourService.reactToArchiveRequest(joarkData.eksternReferanseId)

		val data = createJoarkDbData(joarkData)
		joarkRepository.save(data)

		val response = createResponse(joarkData, data)
		return behaviourService.alterResponse(joarkData.eksternReferanseId, response)
	}

	private fun createResponse(joarkData: JoarkData, data: JoarkDbData): OpprettJournalpostResponse {
		val dokumenter = joarkData.dokumenter.map { Dokumenter(it.brevkode, UUID.randomUUID().toString(), it.tittel) }
		return OpprettJournalpostResponse(dokumenter, data.id, true, "MIDLERTIDIG", "null")
	}

	fun lookup(id: String): Optional<JoarkDbData> {
		return joarkRepository.findById(id)
	}

	private fun createJoarkDbData(joarkData: JoarkData) =
		JoarkDbData(joarkData.eksternReferanseId, joarkData.tittel, joarkData.tema, LocalDateTime.now())
}
