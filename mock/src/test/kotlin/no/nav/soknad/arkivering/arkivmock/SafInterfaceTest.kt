package no.nav.soknad.arkivering.arkivmock

import io.mockk.verify
import no.nav.soknad.arkivering.arkivmock.rest.ArkivRestInterface
import no.nav.soknad.arkivering.arkivmock.rest.GraphQLRequest
import no.nav.soknad.arkivering.arkivmock.rest.SafRestInterface
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


	@Test
	fun `verify SAF rest response - Will return not found`() {
		val eksternReferanseId = UUID.randomUUID().toString()
		val response = safInterface.getJournalpost(
			xCallId = "12345", xCorrelationId = "123456", xNavUserId = "soknadsarkiverer",
			request = GraphQLRequest(HENT_JOURNALPOST_GITT_EKSTERN_REFERANSE_ID, "HentJournalpostGittEksternReferanseId", mapOf("eksternReferanseId" to eksternReferanseId))
			)

		Assertions.assertEquals(HttpStatus.OK, response.statusCode)
		Assertions.assertTrue(response.body!!.contains("{\"data\":null,\"errors\":null,\"extensions\":null}"))
	}

}
