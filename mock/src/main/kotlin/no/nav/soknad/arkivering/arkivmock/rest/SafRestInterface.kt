package no.nav.soknad.arkivering.arkivmock.rest

import no.nav.security.token.support.core.api.Protected
import no.nav.soknad.arkivering.arkivmock.config.Constants.CORRELATION_ID
import no.nav.soknad.arkivering.arkivmock.config.Constants.HEADER_CALL_ID
import no.nav.soknad.arkivering.arkivmock.config.Constants.NAV_CONSUMER_ID
import no.nav.soknad.arkivering.arkivmock.service.SafMockService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMapAdapter
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class SafRestInterface(val safMockService: SafMockService) {

	private val logger = LoggerFactory.getLogger(javaClass)

	@PostMapping(value =  ["/graphql", "/graphql/"])
	fun getJournalpost(
		@RequestHeader(value = CORRELATION_ID, required = false) xCorrelationId: String?,
		@RequestHeader(value = HEADER_CALL_ID, required = false) xCallId: String?,
		@RequestHeader(value = NAV_CONSUMER_ID, required = false) xNavUserId: String?,
		@RequestBody request: GraphQLRequest): ResponseEntity<String> {

		val key = request.variables().get("eksternReferanseId")
		logger.info("$key: SAF request journalpost")

		val keyValueList = mapOf("Content-Type" to mutableListOf(MediaType.APPLICATION_JSON_VALUE))
		val mvm = MultiValueMapAdapter(keyValueList)

		return ResponseEntity(safMockService.get(key as String),mvm, HttpStatus.OK)
	}
}
