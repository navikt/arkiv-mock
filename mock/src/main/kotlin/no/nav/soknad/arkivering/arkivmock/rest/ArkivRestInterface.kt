package no.nav.soknad.arkivering.arkivmock.rest

import no.nav.security.token.support.core.api.Protected
import no.nav.soknad.arkivering.arkivmock.dto.ArkivData
import no.nav.soknad.arkivering.arkivmock.service.ArkivMockService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rest/journalpostapi/v1")
@Protected
class ArkivRestInterface(private val arkivMockService: ArkivMockService) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@PostMapping(value = ["/journalpost"])
	fun receiveJournalpost(@RequestBody arkivData: ArkivData): ResponseEntity<String> {

		val key = arkivData.eksternReferanseId
		val kanal = arkivData.kanal
		logger.info("$key: Received journalpost på kanal $kanal")

		val responseBody = arkivMockService.archive(key, arkivData)
		return ResponseEntity(responseBody, HttpStatus.OK)
	}
}
