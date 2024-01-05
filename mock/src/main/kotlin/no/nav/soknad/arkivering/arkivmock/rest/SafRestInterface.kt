package no.nav.soknad.arkivering.arkivmock.rest

import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import no.nav.soknad.arkivering.arkivmock.config.Constants.CORRELATION_ID
import no.nav.soknad.arkivering.arkivmock.config.Constants.HEADER_CALL_ID
import no.nav.soknad.arkivering.arkivmock.config.Constants.NAV_CONSUMER_ID
import no.nav.soknad.arkivering.arkivmock.service.SafMockService
import org.springframework.web.bind.annotation.RequestHeader

@RestController
@Protected
class SafRestInterface(val safMockService: SafMockService) {

	private val logger = LoggerFactory.getLogger(javaClass)

	private final val X_CORRELATION_ID: String  = "x_correlationId"

	@PostMapping(value =  ["/graphql", "/graphql/"])
		fun getJournalpost(
			@RequestHeader(value = CORRELATION_ID, required = false) xCorrelationId: String?,
			@RequestHeader(value = HEADER_CALL_ID, required = false) xCallId: String?,
			@RequestHeader(value = NAV_CONSUMER_ID, required = false) xNavUserId: String?,
			@RequestBody request: GraphQLRequest): ResponseEntity<String> {

		val key = request.variables().get("eksternReferanseId").toString()
		logger.info("$key: SAF request journalpost")

		return ResponseEntity(safMockService.get(key), HttpStatus.OK)
	}
}
