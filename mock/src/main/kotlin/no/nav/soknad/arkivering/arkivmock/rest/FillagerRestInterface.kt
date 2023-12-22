package no.nav.soknad.arkivering.arkivmock.rest

import no.nav.security.token.support.core.api.Protected
import no.nav.soknad.arkivering.arkivmock.service.BehaviourService
import no.nav.soknad.arkivering.arkivmock.service.FileMockService
import org.springframework.web.bind.annotation.RestController
import no.nav.soknad.innsending.api.InnsendteApi
import no.nav.soknad.innsending.model.AktivSakDto
import no.nav.soknad.innsending.model.SoknadFile
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


@RestController
@Protected
class InnsendingApiRestInterface(val fileMockService: FileMockService): InnsendteApi
{
	private val logger = LoggerFactory.getLogger(javaClass)

	override fun aktiveSaker(): ResponseEntity<List<AktivSakDto>> {
		logger.info("Kall for å hente innsendte søknader for en bruker")

		val innsendteSoknader:List<AktivSakDto> = listOf()
		logger.info("Hentet ${innsendteSoknader.size} innsendteSoknader.")
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(innsendteSoknader)
	}

	override fun hentInnsendteFiler(uuids: List<String>, xInnsendingId: String): ResponseEntity<List<SoknadFile>> {
		logger.info("$xInnsendingId: Kall for å hente filene $uuids til en innsendt søknad")

		val innsendteFiler  = mutableListOf<SoknadFile>()
		uuids.forEach { innsendteFiler.add(fileMockService.getFile(it)) }
		logger.info(
			"$xInnsendingId: Status for henting av følgende innsendte filer ${
				innsendteFiler.map { it.id + ":" + it.fileStatus + ":size=" + it.content?.size }.toList()
			}"
		)
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(innsendteFiler)
	}


}
