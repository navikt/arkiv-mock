package no.nav.soknad.archiving.joarkmock.rest

import no.nav.soknad.archiving.dto.JoarkData
import no.nav.soknad.archiving.joarkmock.dto.JoarkDbData
import no.nav.soknad.archiving.joarkmock.service.JoarkMongoService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/joark")
class JoarkRestInterface(private val joarkMongoService: JoarkMongoService) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@PostMapping(value = ["/save"])
	fun receiveMessage(@Valid @RequestBody joarkData: JoarkData): ResponseEntity<String> {
		logger.info("Received message: '$joarkData'")

		joarkMongoService.archive(joarkData.id, joarkData.message)
		return ResponseEntity("", HttpStatus.OK)
	}

	@GetMapping("/lookup/{name}")
	fun lookup(@PathVariable("name") name: String): List<JoarkDbData> {
		logger.info("Looking up '$name'")
		return joarkMongoService.lookup(name)
	}
}
