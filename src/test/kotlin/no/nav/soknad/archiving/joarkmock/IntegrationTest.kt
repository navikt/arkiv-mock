package no.nav.soknad.archiving.joarkmock

import no.nav.soknad.archiving.dto.Bruker
import no.nav.soknad.archiving.dto.JoarkData
import no.nav.soknad.archiving.joarkmock.rest.BehaviourMocking
import no.nav.soknad.archiving.joarkmock.rest.JoarkRestInterface
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest
class IntegrationTest {

	@Autowired
	private lateinit var joarkRestInterface: JoarkRestInterface

	@Autowired
	private lateinit var behaviourMocking: BehaviourMocking

	@Test
	fun `Will save to database when receiving message`() {
		val id = UUID.randomUUID().toString()
		behaviourMocking.setNormalResponseBehaviour(id)
		val message = "apa bepa"

		joarkRestInterface.receiveMessage(createRequestData(id, message))

		val result = joarkRestInterface.lookup(id)
		assertEquals(1, result.size)
		assertEquals(id, result[0].name)
		assertEquals(message, result[0].message)
	}

	@Test
	fun `Lookup returns empty list if not found`() {
		val result = joarkRestInterface.lookup("lookup key that is not in the database")

		assertTrue(result.isEmpty())
	}

	private fun createRequestData(personId: String, tema: String) =
		JoarkData(Bruker(personId, "FNR"), LocalDate.now().format(DateTimeFormatter.ISO_DATE), emptyList(),
			personId, "INNGAAENDE", "NAV_NO", tema, "tittel")
}
