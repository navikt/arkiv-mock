package no.nav.soknad.archiving.joarkmock

import no.nav.soknad.archiving.dto.JoarkData
import no.nav.soknad.archiving.joarkmock.rest.JoarkRestInterface
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class IntegrationTest {

	@Autowired
	private lateinit var joarkRestInterface: JoarkRestInterface

	@Test
	fun `Will save to database when receiving message`() {
		val id = "Apa bepa"
		val message = "cepa depa"

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

	private fun createRequestData(eksternReferanseId: String, tema: String) =
		JoarkData(eksternReferanseId, "personId", "FNR", tema, LocalDateTime.now(), emptyList(), emptyList())
}
