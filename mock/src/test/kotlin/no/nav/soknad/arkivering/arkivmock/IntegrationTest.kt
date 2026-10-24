package no.nav.soknad.arkivering.arkivmock

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.slot
import io.mockk.verify
import no.nav.soknad.arkivering.arkivmock.dto.ArkivData
import no.nav.soknad.arkivering.arkivmock.dto.ArchiveDocument
import no.nav.soknad.arkivering.arkivmock.dto.ArchiveEntity
import no.nav.soknad.arkivering.arkivmock.dto.AvsenderMottaker
import no.nav.soknad.arkivering.arkivmock.dto.Bruker
import no.nav.soknad.arkivering.arkivmock.dto.Dokument
import no.nav.soknad.arkivering.arkivmock.dto.DokumentVariant
import no.nav.soknad.arkivering.arkivmock.rest.ArkivRestInterface
import no.nav.soknad.arkivering.arkivmock.rest.BehaviourMocking
import no.nav.soknad.arkivering.arkivmock.service.ArkivMockService
import no.nav.soknad.arkivering.arkivmock.service.BehaviourService
import no.nav.soknad.arkivering.arkivmock.service.kafka.KafkaPublisher
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest(properties = ["PUBLISH_ARCHIVE_DOCUMENT_METADATA=true"])
class IntegrationTest {
	private val tema = "Tema"
	private val title = "Title"
	private val kanal = "NAV_NO"

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

		arkivRestInterface.receiveJournalpost(createRequestData(id, dokumenter = testDokumenter()))

		val captor = slot<ArchiveEntity>()
		verify(exactly = 1) { kafkaPublisher.putDataOnTopic(eq(id), capture(captor), any()) }
		val result = captor.captured
		assertEquals(id, result.id)
		assertEquals(tema, result.tema)
		assertEquals(kanal, result.kanal)
		assertEquals(title, result.title)
		assertEquals(null, result.overstyrInnsynsregler)
		assertEquals(
			listOf(
				ArchiveDocument("Syntetisk hoveddokument", "TEST-HOVED", listOf("ARKIV", "ORIGINAL"), 2),
				ArchiveDocument("Syntetisk vedlegg", null, listOf("ORIGINAL")),
				ArchiveDocument("Syntetisk énsidesdokument", null, listOf("ARKIV"), 1),
				ArchiveDocument("Syntetisk ugyldig PDF", null, listOf("ARKIV"))
			),
			result.dokumenter
		)
		val json = result.toString()
		assertTrue(json.contains("Syntetisk hoveddokument"))
		assertTrue(json.contains("TEST-HOVED"))
		assertTrue(json.contains("ARKIV"))
		assertFalse(json.contains("hoved.pdf"))
		assertFalse(json.contains("vedlegg.pdf"))
		assertFalse(json.contains("fysiskDokument"))
		assertFalse(json.contains("filnavn"))
		assertTrue(result.timesaved >= timeWhenStarting)
		assertTrue(result.timesaved <= LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())
	}

	@Test
	fun `Will broadcast overstyrInnsynsregler when receiving message`() {
		val id = UUID.randomUUID().toString()
		behaviourMocking.setNormalResponseBehaviour(id)

		arkivRestInterface.receiveJournalpost(createRequestData(id, "VISES_MASKINELT_GODKJENT"))

		val captor = slot<ArchiveEntity>()
		verify(exactly = 1) { kafkaPublisher.putDataOnTopic(eq(id), capture(captor), any()) }
		assertEquals("VISES_MASKINELT_GODKJENT", captor.captured.overstyrInnsynsregler)
		assertEquals(emptyList<ArchiveDocument>(), captor.captured.dokumenter)
	}

	@Test
	fun `default archive entity JSON omits document metadata`() {
		val entity = ArchiveEntity(
			id = "synthetic-id",
			title = title,
			tema = tema,
			kanal = kanal,
			timesaved = 0
		)

		assertFalse(entity.toString().contains("\"dokumenter\""))
	}

	@Test
	fun `service omits document metadata unless explicitly enabled`() {
		val id = UUID.randomUUID().toString()
		val behaviourService = BehaviourService(ObjectMapper())
		behaviourService.setNormalBehaviour(id)
		val service = ArkivMockService(behaviourService, kafkaPublisher)

		service.archive(id, createRequestData(id, dokumenter = testDokumenter()))

		val captor = slot<ArchiveEntity>()
		verify(exactly = 1) { kafkaPublisher.putDataOnTopic(eq(id), capture(captor), any()) }
		assertEquals(null, captor.captured.dokumenter)
		assertFalse(captor.captured.toString().contains("\"dokumenter\""))
	}

	private fun createRequestData(
		id: String,
		overstyrInnsynsregler: String? = null,
		dokumenter: List<Dokument> = emptyList()
	) =
		ArkivData(
			AvsenderMottaker("12345678901", "FNR"), Bruker("12345678901", "FNR"), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), dokumenter,
			id, "INNGAAENDE", kanal, tema, title, overstyrInnsynsregler
		)

	private fun testDokumenter() = listOf(
		Dokument(
			tittel = "Syntetisk hoveddokument",
			brevkode = "TEST-HOVED",
			dokumentvarianter = listOf(
				DokumentVariant("hoved.pdf", "Application/PdF", syntheticPdf(2), "ARKIV"),
				DokumentVariant("hoved-original.json", "application/json", syntheticPdf(1), "ORIGINAL")
			)
		),
		Dokument(
			tittel = "Syntetisk vedlegg",
			dokumentvarianter = listOf(
				DokumentVariant("vedlegg.json", "application/json", byteArrayOf(3), "ORIGINAL")
			)
		),
		Dokument(
			tittel = "Syntetisk énsidesdokument",
			dokumentvarianter = listOf(
				DokumentVariant("enside.pdf", "pDfA", syntheticPdf(1), "ARKIV")
			)
		),
		Dokument(
			tittel = "Syntetisk ugyldig PDF",
			dokumentvarianter = listOf(
				DokumentVariant("ugyldig.pdf", "application/pdf", byteArrayOf(1, 2, 3), "ARKIV")
			)
		)
	)

	private fun syntheticPdf(numberOfPages: Int): ByteArray =
		PDDocument().use { document ->
			repeat(numberOfPages) { document.addPage(PDPage()) }
			ByteArrayOutputStream().use { output ->
				document.save(output)
				output.toByteArray()
			}
		}
}
