package no.nav.soknad.archiving.joarkmock.rest

import no.nav.soknad.archiving.dto.JoarkData
import no.nav.soknad.archiving.joarkmock.dto.JoarkDbData
import no.nav.soknad.archiving.joarkmock.service.JoarkMockService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/joark")
class JoarkRestInterface(private val joarkMockService: JoarkMockService) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@PostMapping(value = ["/save"])
	fun receiveMessage(@RequestBody joarkData: JoarkData): ResponseEntity<String> {
		logger.info("Received message: '$joarkData'")

		val responseBody = joarkMockService.archive(joarkData)
		return ResponseEntity(responseBody, HttpStatus.OK)
	}

	@GetMapping("/lookup/{id}")
	fun lookup(@PathVariable("id") id: String): JoarkDbData {
		logger.info("Looking up '$id'")
		val response = joarkMockService.lookup(id)

		if (response.isPresent) {
			return response.get()
		} else {
			throw ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to find $id")
		}
	}
}
