package no.nav.soknad.arkivering.arkivmock.rest

import no.nav.soknad.arkivering.arkivmock.config.AppConfiguration
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class HealthInterface(private val appConfiguration: AppConfiguration) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@GetMapping(value = ["/isAlive"])
	fun isAlive(): ResponseEntity<String> {
		logger.info("/isAlive called")

		return if (appConfiguration.applicationState.alive)
			ResponseEntity("Application is alive!", HttpStatus.OK)
		else
			ResponseEntity("Application is NOT alive!", HttpStatus.INTERNAL_SERVER_ERROR)
	}
}
