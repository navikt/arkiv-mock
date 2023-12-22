package no.nav.soknad.arkivering.arkivmock

import no.nav.soknad.arkivering.arkivmock.rest.FileFetchBehaviour
import no.nav.soknad.arkivering.arkivmock.rest.InnsendingApiRestInterface
import no.nav.soknad.arkivering.arkivmock.service.FileResponses
import no.nav.soknad.innsending.model.SoknadFile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.*

@SpringBootTest
class FileFetchTest {

	@Autowired
	private lateinit var fileFetchBehaviour: FileFetchBehaviour
	@Autowired
	private lateinit var fillagerRestInterface: InnsendingApiRestInterface

	@Test
	fun `Responds with file status ok and default file when no previous behaviour is specified`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()

		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)

		Assertions.assertEquals(HttpStatus.OK, response.statusCode)
		Assertions.assertTrue(response.body != null)
		val files = response.body
		if (files == null || files.isEmpty()) {
			Assertions.assertTrue(false, "Expected body")
		} else {
			Assertions.assertEquals(1, files.size)
			val fileResponse = files.get(0)
			Assertions.assertEquals(fileId, fileResponse.id)
			Assertions.assertEquals(SoknadFile.FileStatus.ok, fileResponse.fileStatus)
		}
	}


	@Test
	fun `Responds with file status deleted when this behaviour is set`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()
		val fileId2 = UUID.randomUUID().toString()
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.DELETED.name)

		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId, fileId2), key)

		Assertions.assertEquals(HttpStatus.OK, response.statusCode)
		Assertions.assertTrue(response.body != null)
		val files = response.body
		if (files == null || files.isEmpty() || files.size != 2) {
			Assertions.assertTrue(false, "Expected 2 soknadFiles in response")
		} else {
			val deletedFile = files.filter { it.fileStatus == SoknadFile.FileStatus.deleted }.firstOrNull()
			Assertions.assertEquals(fileId, deletedFile?.id)
			Assertions.assertTrue(deletedFile?.content == null)
			val okFile = files.filter { it.fileStatus == SoknadFile.FileStatus.ok }.firstOrNull()
			Assertions.assertEquals(fileId2, okFile?.id)
			Assertions.assertTrue(okFile?.content != null)
		}
	}


	@Test
	fun `Responds with file status not-found when this behaviour is set`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()
		val fileId2 = UUID.randomUUID().toString()
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.NOT_FOUND.name)

		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId, fileId2), key)

		Assertions.assertEquals(HttpStatus.OK, response.statusCode)
		Assertions.assertTrue(response.body != null)
		val files = response.body
		if (files == null || files.isEmpty() || files.size != 2) {
			Assertions.assertTrue(false, "Expected 2 soknadFiles in response")
		} else {
			val notFoundFile = files.filter { it.fileStatus == SoknadFile.FileStatus.notfound }.firstOrNull()
			Assertions.assertEquals(fileId, notFoundFile?.id)
			Assertions.assertTrue(notFoundFile?.content == null)
			val okFile = files.filter { it.fileStatus == SoknadFile.FileStatus.ok }.firstOrNull()
			Assertions.assertEquals(fileId2, okFile?.id)
			Assertions.assertTrue(okFile?.content != null)
		}
	}


	@Test
	fun `Responds with file status deleted the first two calls then ok`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()
		val noOfFailedAttempts = 2
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.DELETED.name, noOfFailedAttempts)

		val responses = mutableListOf<ResponseEntity<List<SoknadFile>>>()
		repeat(noOfFailedAttempts+1) {
			responses.add( fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key))
		}
		var count = 0
		repeat(noOfFailedAttempts) {
			val response = responses.get(count)
			Assertions.assertEquals(HttpStatus.OK, response.statusCode)
			Assertions.assertTrue(response.body != null)
			val files = response.body
			if (files == null || files.isEmpty() || files.size != 1) {
				Assertions.assertTrue(false, "Expected 1 soknadFile in response")
			} else {
				val notFoundFile = files.filter { it.fileStatus == SoknadFile.FileStatus.deleted }.firstOrNull()
				Assertions.assertEquals(fileId, notFoundFile?.id)
				Assertions.assertTrue(notFoundFile?.content == null)
			}

			count += 1
		}

		val response = responses.get(noOfFailedAttempts)
		Assertions.assertEquals(HttpStatus.OK, response.statusCode)
		Assertions.assertTrue(response.body != null)
		val files = response.body
		if (files == null || files.isEmpty() || files.size != 1) {
			Assertions.assertTrue(false, "Expected 1 soknadFile in response")
		} else {
			val notFoundFile = files.filter { it.fileStatus == SoknadFile.FileStatus.ok }.firstOrNull()
			Assertions.assertEquals(fileId, notFoundFile?.id)
			Assertions.assertTrue(notFoundFile?.content != null)
		}
	}

	@Test
	fun `Responds with file status not-found the first two calls then ok`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()
		val noOfFailedAttempts: Int = 2
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.NOT_FOUND.name, noOfFailedAttempts)

		val responses = mutableListOf<ResponseEntity<List<SoknadFile>>>()
		repeat(noOfFailedAttempts+1) {
			responses.add( fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key))
		}
		var count: Int = 0
		repeat(noOfFailedAttempts) {
			val response = responses.get(count)
			Assertions.assertEquals(HttpStatus.OK, response.statusCode)
			Assertions.assertTrue(response.body != null)
			val files = response.body
			if (files == null || files.isEmpty() || files.size != 1) {
				Assertions.assertTrue(false, "Expected 1 soknadFile in response")
			} else {
				val notFoundFile = files.filter { it.fileStatus == SoknadFile.FileStatus.notfound }.firstOrNull()
				Assertions.assertEquals(fileId, notFoundFile?.id)
				Assertions.assertTrue(notFoundFile?.content == null)
			}

			count += 1
		}

		val response = responses.get(noOfFailedAttempts)
		Assertions.assertEquals(HttpStatus.OK, response.statusCode)
		Assertions.assertTrue(response.body != null)
		val files = response.body
		if (files == null || files.isEmpty() || files.size != 1) {
			Assertions.assertTrue(false, "Expected 1 soknadFile in response")
		} else {
			val notFoundFile = files.filter { it.fileStatus == SoknadFile.FileStatus.ok }.firstOrNull()
			Assertions.assertEquals(fileId, notFoundFile?.id)
			Assertions.assertTrue(notFoundFile?.content != null)
		}
	}

}
