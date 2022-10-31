package no.nav.soknad.arkivering.arkivmock

import com.ninjasquad.springmockk.MockkBean
import io.mockk.slot
import io.mockk.verify
import no.nav.soknad.arkivering.arkivmock.dto.ArkivData
import no.nav.soknad.arkivering.arkivmock.dto.ArchiveEntity
import no.nav.soknad.arkivering.arkivmock.dto.Bruker
import no.nav.soknad.arkivering.arkivmock.rest.ArkivRestInterface
import no.nav.soknad.arkivering.arkivmock.rest.BehaviourMocking
import no.nav.soknad.arkivering.arkivmock.service.kafka.KafkaPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest
class IntegrationTest {
	private val tema = "Tema"
	private val title = "Title"

	@Autowired
	private lateinit var arkivRestInterface: ArkivRestInterface
	@Autowired
	private lateinit var behaviourMocking: BehaviourMocking
	@MockkBean(relaxed = true)
	private lateinit var kafkaPublisher: KafkaPublisher

	@Test
	fun `Will broadcast on Kafka when receiving message`() {
		val timeWhenStarting = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
		val id = UUID.randomUUID().toString()
		behaviourMocking.setNormalResponseBehaviour(id)

		arkivRestInterface.receiveJournalpost(createRequestData(id))

		val captor = slot<ArchiveEntity>()
		verify(exactly = 1) { kafkaPublisher.putDataOnTopic(eq(id), capture(captor), any()) }
		val result = captor.captured
		assertEquals(id, result.id)
		assertEquals(tema, result.tema)
		assertEquals(title, result.title)
		assertTrue(result.timesaved >= timeWhenStarting)
		assertTrue(result.timesaved <= LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())
	}

	private fun createRequestData(id: String) =
		ArkivData(
			Bruker("12345678901", "FNR"), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), emptyList(),
			id, "INNGAAENDE", "NAV_NO", tema, title
		)
}
