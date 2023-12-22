package no.nav.soknad.arkivering.arkivmock.rest

import no.nav.soknad.arkivering.arkivmock.service.BehaviourService
import no.nav.soknad.arkivering.arkivmock.service.FileMockService
import no.nav.soknad.arkivering.arkivmock.service.FileResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/arkiv-mock/response-behaviour")
class BehaviourMocking(val behaviourService: BehaviourService, val fileMockService: FileMockService) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@PutMapping("/mock-response/{key}/{statusCode}/{forAttempts}")
	fun mockResponseBehaviour(
		@PathVariable("key") key: String,
		@PathVariable("statusCode") statusCode: Int,
		@PathVariable("forAttempts") forAttempts: Int
	): ResponseEntity<String> {
		logger.debug("$key: http status code $statusCode will be returned $forAttempts times")

		behaviourService.mockException(key, statusCode, forAttempts)

		return ResponseEntity(HttpStatus.OK)
	}

	@PutMapping("/set-status-ok-with-erroneous-body/{key}/{forAttempts}")
	fun mockOkResponseWithErroneousBody(
		@PathVariable("key") key: String,
		@PathVariable("forAttempts") forAttempts: Int
	): ResponseEntity<String> {
		logger.debug("$key: http status code ${HttpStatus.OK}, but with erroneous body will be returned $forAttempts times")

		behaviourService.mockResponseWithErroneousBody(key, forAttempts)

		return ResponseEntity(HttpStatus.OK)
	}

	@PutMapping("/set-normal-behaviour/{key}")
	fun setNormalResponseBehaviour(@PathVariable("key") key: String): ResponseEntity<String> {
		logger.debug("$key: will accept all calls and return http status ${HttpStatus.OK}")

		behaviourService.setNormalBehaviour(key)

		return ResponseEntity(HttpStatus.OK)
	}

}
