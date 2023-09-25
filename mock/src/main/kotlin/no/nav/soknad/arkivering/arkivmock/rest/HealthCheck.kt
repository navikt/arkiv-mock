package no.nav.soknad.arkivering.arkivmock.rest

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
@Unprotected
class HealthCheck {

	@GetMapping("/isAlive")
	fun isAlive() = ResponseEntity("Application is alive!", HttpStatus.OK)

	@GetMapping("/internal/isAlive")
	fun isAliveInternal() = isAlive()
}
