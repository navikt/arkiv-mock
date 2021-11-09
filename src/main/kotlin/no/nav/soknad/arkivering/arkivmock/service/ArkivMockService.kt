package no.nav.soknad.arkivering.arkivmock.service

import no.nav.soknad.arkivering.arkivmock.dto.ArkivData
import no.nav.soknad.arkivering.arkivmock.dto.ArkivDbData
import no.nav.soknad.arkivering.arkivmock.dto.Dokumenter
import no.nav.soknad.arkivering.arkivmock.dto.OpprettJournalpostResponse
import no.nav.soknad.arkivering.arkivmock.repository.ArkivRepository
import no.nav.soknad.arkivering.arkivmock.service.kafka.KafkaPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class ArkivMockService(
	private val arkivRepository: ArkivRepository,
	private val behaviourService: BehaviourService,
	private val kafkaPublisher: KafkaPublisher
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun reset() {
		arkivRepository.deleteAll()
	}

	fun archive(key: String, arkivData: ArkivData): String? {
		reactToArchiveRequest(key)

		val data = createArkivDbData(key, arkivData)
		saveToDatabaseAndAlertOnKafka(key, data)

		val response = createResponse(arkivData, data)
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

	private fun createResponse(arkivData: ArkivData, data: ArkivDbData): OpprettJournalpostResponse {
		val dokumenter = arkivData.dokumenter.map { Dokumenter(it.brevkode, UUID.randomUUID().toString(), it.tittel) }
		return OpprettJournalpostResponse(dokumenter, data.id, true, "MIDLERTIDIG", "null")
	}

	private fun createArkivDbData(key: String, arkivData: ArkivData): ArkivDbData {
		return ArkivDbData(
			key,
			arkivData.tittel,
			arkivData.tema,
			LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
		)
	}

	private fun saveToDatabaseAndAlertOnKafka(key: String, data: ArkivDbData) {
		val dbEntity = arkivRepository.save(data)
		try {
			kafkaPublisher.putDataOnTopic(key, dbEntity)
		} catch (e: Exception) {
			logger.error("$key: Failed to publish data to Kafka topic!", e)
		}
	}
}
