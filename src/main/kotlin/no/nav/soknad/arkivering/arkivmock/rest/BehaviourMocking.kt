package no.nav.soknad.arkivering.arkivmock.rest

import no.nav.soknad.arkivering.arkivmock.service.BehaviourService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/arkiv-mock/response-behaviour")
class BehaviourMocking(val behaviourService: BehaviourService) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@PutMapping("/mock-response/{key}/{statusCode}/{forAttempts}")
	fun mockResponseBehaviour(
		@PathVariable("key") key: String,
		@PathVariable("statusCode") statusCode: Int,
		@PathVariable("forAttempts") forAttempts: Int
	): ResponseEntity<String> {
		logger.debug("$key: http status code $statusCode will be returned $forAttempts times")

		behaviourService.mockException(key, statusCode, forAttempts)

		return ResponseEntity("", HttpStatus.OK)
	}

	@PutMapping("/set-status-ok-with-erroneous-body/{key}/{forAttempts}")
	fun mockOkResponseWithErroneousBody(
		@PathVariable("key") key: String,
		@PathVariable("forAttempts") forAttempts: Int
	): ResponseEntity<String> {
		logger.debug("$key: http status code ${HttpStatus.OK}, but with erroneous body will be returned $forAttempts times")

		behaviourService.mockResponseWithErroneousBody(key, forAttempts)

		return ResponseEntity("", HttpStatus.OK)
	}

	@PutMapping("/set-normal-behaviour/{key}")
	fun setNormalResponseBehaviour(@PathVariable("key") key: String): ResponseEntity<String> {
		logger.debug("$key: will accept all calls and return http status ${HttpStatus.OK}")

		behaviourService.setNormalBehaviour(key)

		return ResponseEntity("", HttpStatus.OK)
	}

	@PutMapping("/reset/{key}")
	fun resetMockResponseBehaviour(@PathVariable("key") key: String) = setNormalResponseBehaviour(key)

	@GetMapping("/number-of-calls/{key}")
	fun getNumberOfCalls(@PathVariable("key") key: String): Int {
		val numberOfCallsThatHaveBeenMade = behaviourService.getNumberOfCallsThatHaveBeenMade(key)

		logger.info("$key: There have been made $numberOfCallsThatHaveBeenMade calls to save to the archive for this key")

		return numberOfCallsThatHaveBeenMade
	}
}
