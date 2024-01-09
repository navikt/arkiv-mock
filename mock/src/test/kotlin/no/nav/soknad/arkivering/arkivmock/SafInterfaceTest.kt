package no.nav.soknad.arkivering.arkivmock

import no.nav.soknad.arkivering.arkivmock.rest.GraphQLRequest
import no.nav.soknad.arkivering.arkivmock.rest.SafFetchBehaviour
import no.nav.soknad.arkivering.arkivmock.rest.SafRestInterface
import no.nav.soknad.arkivering.arkivmock.service.SafResponses
import no.nav.soknad.arkivering.saf.generated.HENT_JOURNALPOST_GITT_EKSTERN_REFERANSE_ID
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import java.util.*

@SpringBootTest
class SafInterfaceTest {

	@Autowired
	private lateinit var safInterface: SafRestInterface

	@Autowired
	private lateinit var safFetchBehaviour: SafFetchBehaviour


	@Test
	fun `verify SAF rest response - Will return not found`() {
		val eksternReferanseId = UUID.randomUUID().toString()

		// Given default behaviour
		safFetchBehaviour.setSafResponseBehaviour(key = eksternReferanseId, safResponse = SafResponses.NOT_FOUND.name)

		// When
		val response = safInterface.getJournalpost(
			xCallId = "12345", xCorrelationId = "123456", xNavUserId = "soknadsarkiverer",
			request = GraphQLRequest(HENT_JOURNALPOST_GITT_EKSTERN_REFERANSE_ID, "HentJournalpostGittEksternReferanseId", mapOf("eksternReferanseId" to eksternReferanseId))
			)

		// Then
		Assertions.assertEquals(HttpStatus.OK, response.statusCode)
		Assertions.assertTrue(response.body!!.contains("{\"journalpost\":null}"))
	}

	@Test
	fun `verify SAF rest response - Will return found`() {
		val eksternReferanseId = UUID.randomUUID().toString()

		// Given
		safFetchBehaviour.setSafResponseBehaviour(key = eksternReferanseId, safResponse = SafResponses.OK.name)

		// When
		val response = safInterface.getJournalpost(
			xCallId = "12345", xCorrelationId = "123456", xNavUserId = "soknadsarkiverer",
			request = GraphQLRequest(HENT_JOURNALPOST_GITT_EKSTERN_REFERANSE_ID, "HentJournalpostGittEksternReferanseId", mapOf("eksternReferanseId" to eksternReferanseId))
		)

		// Then
		Assertions.assertEquals(HttpStatus.OK, response.statusCode)
		val body = response.body
		Assertions.assertTrue(body != null && body.contains(eksternReferanseId))
	}

	@Test
	fun `verify SAF rest response - first time not-found second time ok`() {
		val eksternReferanseId = UUID.randomUUID().toString()

		// Given
		val noOfNotFoundAttempts = 1
		safFetchBehaviour.setSafResponseBehaviour(key = eksternReferanseId, safResponse = SafResponses.NOT_FOUND.name, noOfNotFoundAttempts)

		// When
		val response1 = safInterface.getJournalpost(
			xCallId = "12345", xCorrelationId = "123456", xNavUserId = "soknadsarkiverer",
			request = GraphQLRequest(HENT_JOURNALPOST_GITT_EKSTERN_REFERANSE_ID, "HentJournalpostGittEksternReferanseId", mapOf("eksternReferanseId" to eksternReferanseId))
		)
		val response2 = safInterface.getJournalpost(
			xCallId = "12345", xCorrelationId = "123456", xNavUserId = "soknadsarkiverer",
			request = GraphQLRequest(HENT_JOURNALPOST_GITT_EKSTERN_REFERANSE_ID, "HentJournalpostGittEksternReferanseId", mapOf("eksternReferanseId" to eksternReferanseId))
		)

		// Then
		Assertions.assertEquals(HttpStatus.OK, response1.statusCode)
		Assertions.assertTrue(response1.body!!.contains("{\"journalpost\":null}"))
		Assertions.assertEquals(HttpStatus.OK, response2.statusCode)
		val body = response2.body
		Assertions.assertTrue(body != null && body.contains(eksternReferanseId))
	}

}
