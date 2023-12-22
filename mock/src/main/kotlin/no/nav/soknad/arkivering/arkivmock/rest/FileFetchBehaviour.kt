package no.nav.soknad.arkivering.arkivmock.rest

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
@RequestMapping("/arkiv-mock")
class FileFetchBehaviour(private val fileMockService: FileMockService) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@PutMapping("/mock-file-response/{key}/{fileResponse}/{forAttempts}")
	fun setFileResponseBehaviour(
		@PathVariable("key") key: String,
		@PathVariable("fileResponse") fileResponse: String = FileResponses.One_MB.name,
		@PathVariable("forAttempts") forAttempts: Int = -1
	): ResponseEntity<String> {

		if (forAttempts > -1) {
			logger.debug("FileId=$key: changed response behaviour. Will respond with ${fileResponse} for the first $forAttempts and then with status ok and default file afterwards")
		} else {
			logger.debug("FileId=$key: changed response behaviour. File fetch will respond with $fileResponse")
		}

		fileMockService.setFileResponse(key, fileResponse, forAttempts)

		return ResponseEntity(HttpStatus.OK)
	}

}
