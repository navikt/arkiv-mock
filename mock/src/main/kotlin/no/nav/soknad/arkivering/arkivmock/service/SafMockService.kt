package no.nav.soknad.arkivering.arkivmock.service

import com.expediagroup.graphql.client.types.GraphQLClientError
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.soknad.arkivering.saf.generated.HentJournalpostGittEksternReferanseId
import no.nav.soknad.arkivering.saf.generated.enums.*
import no.nav.soknad.arkivering.saf.generated.hentjournalpostgitteksternreferanseid.AvsenderMottaker
import no.nav.soknad.arkivering.saf.generated.hentjournalpostgitteksternreferanseid.Bruker
import no.nav.soknad.arkivering.saf.generated.hentjournalpostgitteksternreferanseid.Journalpost
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class SafMockService {
	private val logger = LoggerFactory.getLogger(javaClass)

	private val safBehaviours: MutableMap<String, SafResponse_Behaviour> = mutableMapOf()

	fun setSafResponse(key: String, response: String, forAttempts: Int) {
		safBehaviours.put(key, SafResponse_Behaviour(behaviour = SafResponses.valueOf(response), forAttempts = forAttempts))
	}

	fun getSafResponseBehaviour(key: String): SafResponse_Behaviour {
		return safBehaviours.getOrDefault(key, SafResponse_Behaviour(SafResponses.NOT_FOUND, mockedException = null, forAttempts = -1, calls = 0 ))
	}


	fun get(key: String): String {

		val response = getSafResponseBehaviour(key)
		response.calls += 1
		safBehaviours.put(key, response)

		// For henting av journalpost fra SAF kan det være satt opp at responsen skal gi, enten not-found eller ok, x antall attempt før default respons.
		// Hvis antall kall utført for å hente fil er mindre enn antall attempts spesifisert respons returneres.
		if (response.forAttempts >= response.calls) {
			return when (response.behaviour) {
				SafResponses.NOT_FOUND -> createSafResponse_withoutJournalpost(key)
				SafResponses.OK -> createSafResponse_withJournalpost(key)
			}
		}

		// Hvis det er satt opp at det skal være noen not-found attempts før ok respons, og dette antallet er nådd, inverter response oppførsel
		if (response.forAttempts > -1 )
			response.behaviour = if (response.behaviour == SafResponses.OK) SafResponses.NOT_FOUND else SafResponses.OK

		// Returner iht. konfigureringen i response objektet.
		return when (response.behaviour) {
			SafResponses.NOT_FOUND -> createSafResponse_withoutJournalpost(key)
			SafResponses.OK -> createSafResponse_withJournalpost(key)
		}

	}

	private fun createSafResponse_withJournalpost(innsendingsId: String): String
	{
		logger.info("$innsendingsId: ***SAF returner journalpost****")
		val objectMapper = ObjectMapper()
		return objectMapper.writeValueAsString(
			GraphQlResponse(data = HentJournalpostGittEksternReferanseId.Result(journalpost = buildJournalpost(innsendingsId)), errors = null, extensions = null)
		)
	}

	private fun createSafResponse_withoutJournalpost(innsendingsId: String): String
	{
		logger.info("$innsendingsId: -----SAF returner tom journalpost-----")
		val objectMapper = ObjectMapper()
		return objectMapper.writeValueAsString(
			HentJournalpostGittEksternReferanseId.Result(journalpost = null)
		)
	}

	data class GraphQlResponse(
		override val data: HentJournalpostGittEksternReferanseId.Result? = null,
		override val errors: List<GraphQLClientError>? = null,
		override val extensions: Map<String, Any?>? = null
	): GraphQLClientResponse<HentJournalpostGittEksternReferanseId.Result>

	fun buildJournalpost(key: String): Journalpost {
		return Journalpost(
			journalpostId = "12345",
			tittel = "Test søknad",
			journalposttype = Journalposttype.I,
			journalstatus = Journalstatus.MOTTATT,
			tema = Tema.BID,
			bruker = Bruker("12345678901", BrukerIdType.FNR),
			avsenderMottaker = AvsenderMottaker("12345678901", type = AvsenderMottakerIdType.FNR),
			datoOpprettet = OffsetDateTime.now().toInstant().toString(),
			eksternReferanseId = key
		)

	}

}
