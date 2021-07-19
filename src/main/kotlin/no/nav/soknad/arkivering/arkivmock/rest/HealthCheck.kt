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
class HealthCheck(private val appConfiguration: AppConfiguration) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@GetMapping(value = ["/isAlive"])
	fun isAlive(): ResponseEntity<String> {
		return if (appConfiguration.applicationState.alive)
			ResponseEntity("Application is alive!", HttpStatus.OK)
		else {
			logger.error("Application is NOT alive!")
			ResponseEntity("Application is NOT alive!", HttpStatus.INTERNAL_SERVER_ERROR)
		}
	}

	@GetMapping(value = ["/isReady"])
	fun isReady(): ResponseEntity<String> {
		return if (appConfiguration.applicationState.ready)
			ResponseEntity("Application is ready!", HttpStatus.OK)
		else {
			logger.error("Application is NOT ready!")
			ResponseEntity("Application is NOT ready!", HttpStatus.INTERNAL_SERVER_ERROR)
		}
	}
}
