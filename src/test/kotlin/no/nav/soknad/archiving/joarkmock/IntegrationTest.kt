package no.nav.soknad.archiving.joarkmock

import no.nav.soknad.archiving.dto.Bruker
import no.nav.soknad.archiving.dto.JoarkData
import no.nav.soknad.archiving.joarkmock.rest.BehaviourMocking
import no.nav.soknad.archiving.joarkmock.rest.JoarkRestInterface
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest
class IntegrationTest {
	private val tema = "Tema"
	private val title = "Title"

	@Autowired
	private lateinit var joarkRestInterface: JoarkRestInterface

	@Autowired
	private lateinit var behaviourMocking: BehaviourMocking

	@Test
	fun `Will save to database when receiving message`() {
		val timeWhenStarting = LocalDateTime.now().minusSeconds(1)
		val id = UUID.randomUUID().toString()
		behaviourMocking.setNormalResponseBehaviour(id)

		joarkRestInterface.receiveMessage(createRequestData(id))

		val result = joarkRestInterface.lookup(id)
		assertEquals(id, result.id)
		assertEquals(tema, result.tema)
		assertEquals(title, result.title)
		assertTrue(result.timesaved.isBefore(LocalDateTime.now().plusSeconds(1)))
		assertTrue(result.timesaved.isAfter(timeWhenStarting))
	}

	@Test
	fun `Lookup throws exception if not found`() {
		assertThrows<ResponseStatusException> {
			joarkRestInterface.lookup("lookup key that is not in the database")
		}
	}

	private fun createRequestData(personId: String) =
		JoarkData(Bruker(personId, "FNR"), LocalDate.now().format(DateTimeFormatter.ISO_DATE), emptyList(),
			personId, "INNGAAENDE", "NAV_NO", tema, title)
}
