package no.nav.soknad.arkivering.arkivmock.rest

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.Unprotected
import no.nav.soknad.arkivering.arkivmock.dto.ArkivData
import no.nav.soknad.arkivering.arkivmock.service.ArkivMockService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rest/journalpostapi/v1")
@Protected
class ArkivRestInterface(private val arkivMockService: ArkivMockService) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@PostMapping(value = ["/journalpost"])
	fun receiveJournalpost(
		@RequestHeader("innsendingKey") innsendingKey: String?,
		@RequestBody arkivData: ArkivData
	): ResponseEntity<String> {

		val key = innsendingKey ?: "null"
		logger.info("$key: Received message with id '${arkivData.eksternReferanseId}'")

		val responseBody = arkivMockService.archive(key, arkivData)
		return ResponseEntity(responseBody, HttpStatus.OK)
	}

	@DeleteMapping(value = ["/reset"])
	@Unprotected
	fun reset() {
		logger.info("Will reset database")

		arkivMockService.reset()
	}
}
