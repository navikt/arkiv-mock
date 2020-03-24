package no.nav.soknad.archiving.joarkmock.rest

import no.nav.soknad.archiving.joarkmock.service.BehaviourService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/joark/mock/response-behaviour")
class BehaviourMocking(val behaviourService: BehaviourService) {

	@PutMapping("/{statusCode}/{forAttempts}")
	fun mockResponseBehaviour(@PathVariable("statusCode") statusCode: Int,
														@PathVariable("forAttempts") forAttempts: Int): ResponseEntity<String> {

		behaviourService.mockException(statusCode, forAttempts)

		return ResponseEntity("", HttpStatus.OK)
	}

	@PutMapping("/setStatusOkWithErroneousBody")
	fun mockOkResponseWithErroneousBody(): ResponseEntity<String> {

		behaviourService.mockResponseWithErroneousBody()

		return ResponseEntity("", HttpStatus.OK)
	}

	@PutMapping("/reset")
	fun resetMockResponseBehaviour(): ResponseEntity<String> {

		behaviourService.setNormalBehaviour()

		return ResponseEntity("", HttpStatus.OK)
	}

	@GetMapping("/numberOfCalls")
	fun getNumberOfCalls(): Int {

		return behaviourService.getNumberOfCallsThatHaveBeenMade()
	}
}
