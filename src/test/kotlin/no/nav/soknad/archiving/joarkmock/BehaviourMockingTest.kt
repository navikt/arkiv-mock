package no.nav.soknad.archiving.joarkmock

import no.nav.soknad.archiving.dto.Bruker
import no.nav.soknad.archiving.dto.JoarkData
import no.nav.soknad.archiving.joarkmock.exceptions.InternalServerErrorException
import no.nav.soknad.archiving.joarkmock.exceptions.NotFoundException
import no.nav.soknad.archiving.joarkmock.rest.BehaviourMocking
import no.nav.soknad.archiving.joarkmock.rest.JoarkRestInterface
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest
class BehaviourMockingTest {

	@Autowired
	private lateinit var joarkRestInterface: JoarkRestInterface

	@Autowired
	private lateinit var behaviourMocking: BehaviourMocking

	private val id = UUID.randomUUID().toString()

	@BeforeEach
	fun setup() {
		behaviourMocking.resetMockResponseBehaviour(id)
	}

	@Test
	fun `No specified mock behaviour - Will save to DB`() {
		val response = joarkRestInterface.receiveMessage(createRequestData(id))

		assertEquals(HttpStatus.OK, response.statusCode)
		assertTrue(response.body!!.contains("{\"dokumenter\":[],\"journalpostId\":\""))
		assertTrue(response.body!!.contains("\",\"journalpostferdigstilt\":true,\"journalstatus\":\"MIDLERTIDIG\",\"melding\":\"null\"}"))

		val result = joarkRestInterface.lookup(id)
		assertEquals(1, result.size)

		assertEquals(1, behaviourMocking.getNumberOfCalls(id))
	}

	@Test
	fun `Mock response with Ok Status but wrong body - Will save to DB`() {
		behaviourMocking.mockOkResponseWithErroneousBody(id, 1)

		val response = joarkRestInterface.receiveMessage(createRequestData(id))

		assertEquals(HttpStatus.OK, response.statusCode)
		assertEquals("THIS IS A MOCKED INVALID RESPONSE", response.body)

		val result = joarkRestInterface.lookup(id)
		assertEquals(1, result.size)

		assertEquals(1, behaviourMocking.getNumberOfCalls(id))
	}

	@Test
	fun `Mock Joark responds with status 404 two times, third works - Will save to DB on third attempt`() {
		behaviourMocking.mockResponseBehaviour(id, 404, 2)

		assertThrows<NotFoundException> {
			joarkRestInterface.receiveMessage(createRequestData(id))
		}
		assertEquals(0, joarkRestInterface.lookup(id).size)

		assertThrows<NotFoundException> {
			joarkRestInterface.receiveMessage(createRequestData(id))
		}
		assertEquals(0, joarkRestInterface.lookup(id).size)

		val response = joarkRestInterface.receiveMessage(createRequestData(id))
		assertEquals(HttpStatus.OK, response.statusCode)
		assertEquals(1, joarkRestInterface.lookup(id).size)

		assertEquals(3, behaviourMocking.getNumberOfCalls(id))
	}

	@Test
	fun `Mock Joark responds with status 500 three times, third works - Will save to DB on third attempt`() {
		behaviourMocking.mockResponseBehaviour(id, 500, 3)

		assertThrows<InternalServerErrorException> {
			joarkRestInterface.receiveMessage(createRequestData(id))
		}
		assertEquals(0, joarkRestInterface.lookup(id).size)

		assertThrows<InternalServerErrorException> {
			joarkRestInterface.receiveMessage(createRequestData(id))
		}
		assertEquals(0, joarkRestInterface.lookup(id).size)

		assertThrows<InternalServerErrorException> {
			joarkRestInterface.receiveMessage(createRequestData(id))
		}
		assertEquals(0, joarkRestInterface.lookup(id).size)

		val response = joarkRestInterface.receiveMessage(createRequestData(id))
		assertEquals(HttpStatus.OK, response.statusCode)
		assertEquals(1, joarkRestInterface.lookup(id).size)

		assertEquals(4, behaviourMocking.getNumberOfCalls(id))
	}

	private fun createRequestData(personId: String) =
		JoarkData(Bruker(personId, "FNR"), LocalDate.now().format(DateTimeFormatter.ISO_DATE), emptyList(),
			personId, "INNGAAENDE", "NAV_NO", "tema", "tittel")
}
