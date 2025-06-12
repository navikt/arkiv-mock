package no.nav.soknad.arkivering.arkivmock.service

import no.nav.soknad.arkivering.arkivmock.dto.ArkivData
import no.nav.soknad.arkivering.arkivmock.dto.ArchiveEntity
import no.nav.soknad.arkivering.arkivmock.dto.Dokumenter
import no.nav.soknad.arkivering.arkivmock.dto.OpprettJournalpostResponse
import no.nav.soknad.arkivering.arkivmock.service.kafka.KafkaPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class ArkivMockService(private val behaviourService: BehaviourService, private val kafkaPublisher: KafkaPublisher) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun archive(key: String, arkivData: ArkivData): String? {
		logger.info("$key: Will archive data for user ${arkivData.bruker} - ${arkivData.tittel} (tema ${arkivData.tema})")
		reactToArchiveRequest(key)

		publishReceivedDataOnKafka(key, arkivData)

		val response = createResponse(arkivData)
		return behaviourService.alterResponse(key, response)
	}

	private fun reactToArchiveRequest(key: String) {

		try {
			behaviourService.reactToArchiveRequest(key)
		} finally {
			val numberOfCalls = behaviourService.getNumberOfCallsThatHaveBeenMade(key)
			try {
				kafkaPublisher.putNumberOfCallsOnTopic(key, if (numberOfCalls > 0) numberOfCalls else 1)
			} catch (e: Exception) {
				logger.error("$key: Failed to publish number of calls to Kafka topic!", e)
			}
		}
	}

	private fun createResponse(arkivData: ArkivData): OpprettJournalpostResponse {
		val dokumenter = arkivData.dokumenter.map { Dokumenter(it.brevkode, UUID.randomUUID().toString(), it.tittel) }
		val id = arkivData.eksternReferanseId.reversed() // Simulate that Joark returns a different ID than the external one by reversing
		return OpprettJournalpostResponse(dokumenter, id, true, "MIDLERTIDIG", "null")
	}

	private fun createArchiveEntity(key: String, arkivData: ArkivData): ArchiveEntity {
		return ArchiveEntity(
			key,
			arkivData.tittel,
			arkivData.tema,
			LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
		)
	}

	private fun publishReceivedDataOnKafka(key: String, arkivData: ArkivData) {
		try {
			logger.info("$key: Will publish data to Kafka topic for ${arkivData.bruker} (tema: ${arkivData.tema})")
			val data = createArchiveEntity(key, arkivData)
			kafkaPublisher.putDataOnTopic(key, data)
			logger.info("$key: Published data to Kafka topic for ${arkivData.bruker} (tema: ${arkivData.tema})")
		} catch (e: Exception) {
			logger.error("$key: Failed to publish data to Kafka topic!", e)
		}
	}
}
