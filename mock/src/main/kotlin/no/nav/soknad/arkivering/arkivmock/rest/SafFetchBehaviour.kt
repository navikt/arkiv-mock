package no.nav.soknad.arkivering.arkivmock.rest

import no.nav.soknad.arkivering.arkivmock.service.SafMockService
import no.nav.soknad.arkivering.arkivmock.service.SafResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/arkiv-mock")
class SafFetchBehaviour(val safMockService: SafMockService) {

	private val logger = LoggerFactory.getLogger(javaClass)

	@PutMapping("/mock-saf-response/{key}/{safResponse}/{forAttempts}")
	fun setSafResponseBehaviour(
		@PathVariable("key") key: String,
		@PathVariable("safResponse") safResponse: String = SafResponses.NOT_FOUND.name,
		@PathVariable("forAttempts") forAttempts: Int = -1
	): ResponseEntity<String> {

		if (forAttempts > -1) {
			logger.debug("$key: changed SAF response behaviour. Will respond with ${safResponse} for the first $forAttempts and then with changed status afterwards")
		} else {
			logger.debug("$key: changed SAF response behaviour. SAF fetch will respond with $safResponse")
		}

		safMockService.setSafResponse(key, safResponse, forAttempts)

		return ResponseEntity(HttpStatus.OK)
	}

}
