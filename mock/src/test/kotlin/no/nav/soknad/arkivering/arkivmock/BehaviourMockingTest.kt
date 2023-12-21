package no.nav.soknad.arkivering.arkivmock

import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import no.nav.soknad.arkivering.arkivmock.dto.ArkivData
import no.nav.soknad.arkivering.arkivmock.dto.Bruker
import no.nav.soknad.arkivering.arkivmock.exceptions.InternalServerErrorException
import no.nav.soknad.arkivering.arkivmock.exceptions.NotFoundException
import no.nav.soknad.arkivering.arkivmock.rest.ArkivRestInterface
import no.nav.soknad.arkivering.arkivmock.rest.BehaviourMocking
import no.nav.soknad.arkivering.arkivmock.rest.InnsendingApiRestInterface
import no.nav.soknad.arkivering.arkivmock.service.FileResponses
import no.nav.soknad.arkivering.arkivmock.service.kafka.KafkaPublisher
import no.nav.soknad.innsending.model.SoknadFile
import no.nav.soknad.innsending.model.SoknadsStatusDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest
class BehaviourMockingTest {

	@Autowired
	private lateinit var arkivRestInterface: ArkivRestInterface
	@Autowired
	private lateinit var fillagerRestInterface: InnsendingApiRestInterface
	@Autowired
	private lateinit var behaviourMocking: BehaviourMocking
	@MockkBean(relaxed = true)
	private lateinit var kafkaPublisher: KafkaPublisher

	private val id = UUID.randomUUID().toString()

	@BeforeEach
	fun setup() {
		behaviourMocking.setNormalResponseBehaviour(id)
	}

	@Test
	fun `No specified mock behaviour - Will broadcast on Kafka`() {
		val response = arkivRestInterface.receiveJournalpost(createRequestData(id))

		assertEquals(HttpStatus.OK, response.statusCode)
		assertTrue(response.body!!.contains("{\"dokumenter\":[],\"journalpostId\":\""))
		assertTrue(response.body!!.contains("\",\"journalpostferdigstilt\":true,\"journalstatus\":\"MIDLERTIDIG\",\"melding\":\"null\"}"))
		verify(timeout = 1000, exactly = 1) { kafkaPublisher.putNumberOfCallsOnTopic(eq(id), eq(1), any()) }
		verify(timeout = 1000, exactly = 1) { kafkaPublisher.putDataOnTopic(eq(id), any(), any()) }
	}

	@Test
	fun `Mock response with Ok Status but wrong body - Will broadcast on Kafka`() {
		behaviourMocking.mockOkResponseWithErroneousBody(id, 1)

		val response = arkivRestInterface.receiveJournalpost(createRequestData(id))

		assertEquals(HttpStatus.OK, response.statusCode)
		assertEquals("THIS_IS_A_MOCKED_INVALID_RESPONSE", response.body)
		verify(timeout = 1000, exactly = 1) { kafkaPublisher.putNumberOfCallsOnTopic(eq(id), eq(1), any()) }
		verify(timeout = 1000, exactly = 1) { kafkaPublisher.putDataOnTopic(eq(id), any(), any()) }
	}

	@Test
	fun `Responds with status 404 two times, third time works - Will broadcast on Kafka on third attempt`() {
		behaviourMocking.mockResponseBehaviour(id, 404, 2)

		assertThrows<NotFoundException> {
			arkivRestInterface.receiveJournalpost(createRequestData(id))
		}

		assertThrows<NotFoundException> {
			arkivRestInterface.receiveJournalpost(createRequestData(id))
		}

		val response = arkivRestInterface.receiveJournalpost(createRequestData(id))

		assertEquals(HttpStatus.OK, response.statusCode)
		verify(timeout = 1000, exactly = 1) { kafkaPublisher.putNumberOfCallsOnTopic(eq(id), eq(3), any()) }
		verify(timeout = 1000, exactly = 1) { kafkaPublisher.putDataOnTopic(eq(id), any(), any()) }
	}

	@Test
	fun `Responds with status 500 three times, third time works - Will broadcast on Kafka on third attempt`() {
		behaviourMocking.mockResponseBehaviour(id, 500, 3)

		assertThrows<InternalServerErrorException> {
			arkivRestInterface.receiveJournalpost(createRequestData(id))
		}

		assertThrows<InternalServerErrorException> {
			arkivRestInterface.receiveJournalpost(createRequestData(id))
		}

		assertThrows<InternalServerErrorException> {
			arkivRestInterface.receiveJournalpost(createRequestData(id))
		}

		val response = arkivRestInterface.receiveJournalpost(createRequestData(id))
		assertEquals(HttpStatus.OK, response.statusCode)
		verify(timeout = 1000, exactly = 1) { kafkaPublisher.putNumberOfCallsOnTopic(eq(id), eq(4), any()) }
		verify(timeout = 1000, exactly = 1) { kafkaPublisher.putDataOnTopic(eq(id), any(), any()) }
	}

	@Test
	fun `Responds with file status ok and default file when no previous behaviour is specified`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()

		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)

		assertEquals(HttpStatus.OK, response.statusCode)
		assertTrue(response.body != null)
		val files = response.body
		assertEquals(1, files?.size)
		assertEquals(fileId, files?.first?.id)
		assertEquals(SoknadFile.FileStatus.ok, files?.first?.fileStatus)
	}


	@Test
	fun `Responds with file status deleted when this behaviour is set`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()
		val fileId2 = UUID.randomUUID().toString()
		behaviourMocking.setFileResponseBehaviour(fileId, FileResponses.DELETED.name)

		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId, fileId2), key)

		assertEquals(HttpStatus.OK, response.statusCode)
		assertTrue(response.body != null)
		val files = response.body
		if (files == null || files.isEmpty() || files.size != 2) {
			assertTrue(false, "Expected 2 soknadFiles in response")
		} else {
			val deletedFile = files.filter { it.fileStatus == SoknadFile.FileStatus.deleted }.first
			assertEquals(fileId, deletedFile.id)
			assertTrue(deletedFile.content == null)
			val okFile = files.filter { it.fileStatus == SoknadFile.FileStatus.ok }.first
			assertEquals(fileId2, okFile.id)
			assertTrue(okFile.content != null)
		}
	}


	@Test
	fun `Responds with file status not-found when this behaviour is set`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()
		val fileId2 = UUID.randomUUID().toString()
		behaviourMocking.setFileResponseBehaviour(fileId, FileResponses.NOT_FOUND.name)

		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId, fileId2), key)

		assertEquals(HttpStatus.OK, response.statusCode)
		assertTrue(response.body != null)
		val files = response.body
		if (files == null || files.isEmpty() || files.size != 2) {
			assertTrue(false, "Expected 2 soknadFiles in response")
		} else {
			val notFoundFile = files.filter { it.fileStatus == SoknadFile.FileStatus.notfound }.first
			assertEquals(fileId, notFoundFile.id)
			assertTrue(notFoundFile.content == null)
			val okFile = files.filter { it.fileStatus == SoknadFile.FileStatus.ok }.first
			assertEquals(fileId2, okFile.id)
			assertTrue(okFile.content != null)
		}
	}


	@Test
	fun `Responds with file status deleted the first two calls then ok`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()
		val noOfFailedAttempts: Int = 2
		behaviourMocking.setFileResponseBehaviour(fileId, FileResponses.DELETED.name, noOfFailedAttempts)

		val responses = mutableListOf<ResponseEntity<List<SoknadFile>>>()
		repeat(noOfFailedAttempts+1) {
			responses.add( fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key))
		}
		var count: Int = 0
		repeat(noOfFailedAttempts) {
			val response = responses.get(count)
			assertEquals(HttpStatus.OK, response.statusCode)
			assertTrue(response.body != null)
			val files = response.body
			if (files == null || files.isEmpty() || files.size != 1) {
				assertTrue(false, "Expected 1 soknadFile in response")
			} else {
				val notFoundFile = files.filter { it.fileStatus == SoknadFile.FileStatus.deleted }.first
				assertEquals(fileId, notFoundFile.id)
				assertTrue(notFoundFile.content == null)
			}

			count += 1
		}

		val response = responses.last
		assertEquals(HttpStatus.OK, response.statusCode)
		assertTrue(response.body != null)
		val files = response.body
		if (files == null || files.isEmpty() || files.size != 1) {
			assertTrue(false, "Expected 1 soknadFile in response")
		} else {
			val notFoundFile = files.filter { it.fileStatus == SoknadFile.FileStatus.ok }.first
			assertEquals(fileId, notFoundFile.id)
			assertTrue(notFoundFile.content != null)
		}
	}

	@Test
	fun `Responds with file status not-found the first two calls then ok`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()
		val noOfFailedAttempts: Int = 2
		behaviourMocking.setFileResponseBehaviour(fileId, FileResponses.NOT_FOUND.name, noOfFailedAttempts)

		val responses = mutableListOf<ResponseEntity<List<SoknadFile>>>()
		repeat(noOfFailedAttempts+1) {
			responses.add( fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key))
		}
		var count: Int = 0
		repeat(noOfFailedAttempts) {
			val response = responses.get(count)
			assertEquals(HttpStatus.OK, response.statusCode)
			assertTrue(response.body != null)
			val files = response.body
			if (files == null || files.isEmpty() || files.size != 1) {
				assertTrue(false, "Expected 1 soknadFile in response")
			} else {
				val notFoundFile = files.filter { it.fileStatus == SoknadFile.FileStatus.notfound }.first
				assertEquals(fileId, notFoundFile.id)
				assertTrue(notFoundFile.content == null)
			}

			count += 1
		}

		val response = responses.last
		assertEquals(HttpStatus.OK, response.statusCode)
		assertTrue(response.body != null)
		val files = response.body
		if (files == null || files.isEmpty() || files.size != 1) {
			assertTrue(false, "Expected 1 soknadFile in response")
		} else {
			val notFoundFile = files.filter { it.fileStatus == SoknadFile.FileStatus.ok }.first
			assertEquals(fileId, notFoundFile.id)
			assertTrue(notFoundFile.content != null)
		}
	}

	private fun createRequestData(personId: String) =
		ArkivData(
			Bruker(personId, "FNR"), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), emptyList(),
			personId, "INNGAAENDE", "NAV_NO", "tema", "tittel"
		)
}
