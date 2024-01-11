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

		// When
		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)

		// Then
		assertFileStatus(response, fileId, SoknadFile.FileStatus.ok )
	}


	@Test
	fun `Responds with file status ok and 100KB file when this behaviour is specified`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()

		// Given
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.OneHundred_KB.name)

		// When
		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)

		// Then
		assertFileStatus(response, fileId, SoknadFile.FileStatus.ok )
	}

	@Test
	fun `Responds with file status ok and 10MB file when this behaviour is specified`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()

		// Given
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.Ten_MB.name)

		// When
		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)

		// Then
		assertFileStatus(response, fileId, SoknadFile.FileStatus.ok )
	}


	@Test
	fun `Responds with file status ok and 50MB file when this behaviour is specified`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()

		// given
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.Fifty_50_MB.name)

		// when
		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)

		// then
		assertFileStatus(response, fileId, SoknadFile.FileStatus.ok )

	}


	@Test
	fun `Responds with file status ok and 50MB file on third attempt`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()

		// Given
		val noOfFailedAttempts = 2
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.Fifty_50_MB.name, noOfFailedAttempts)

		// when
		val response1 = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)
		val response2 = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)
		val response3 = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)

		// then
		assertFileStatus(response1, fileId, SoknadFile.FileStatus.notfound)
		assertFileStatus(response2, fileId, SoknadFile.FileStatus.notfound )
		assertFileStatus(response3, fileId, SoknadFile.FileStatus.ok )

	}

	private fun assertFileStatus(
		response1: ResponseEntity<List<SoknadFile>>,
		fileId: String,
		fileStatus: SoknadFile.FileStatus
	) {
		Assertions.assertEquals(HttpStatus.OK, response1.statusCode)
		Assertions.assertTrue(response1.body != null)
		val files = response1.body
		if (files == null || files.isEmpty()) {
			throw RuntimeException("Response body is empty!")
		} else {
			Assertions.assertEquals(1, files.size)
			val fileResponse = files.get(0)
			Assertions.assertEquals(fileId, fileResponse.id)
			Assertions.assertEquals(fileStatus, fileResponse.fileStatus)
		}
	}

	private fun assertResponse(
		response: ResponseEntity<List<SoknadFile>>,
		expectedFileIdStatusMap: Map<String, SoknadFile.FileStatus>
	) {
		Assertions.assertEquals(HttpStatus.OK, response.statusCode)
		Assertions.assertTrue(response.body != null)
		val files = response.body
		if (files == null || files.isEmpty() || files.size != expectedFileIdStatusMap.size) {
			throw java.lang.RuntimeException("Expected ${expectedFileIdStatusMap.size} soknadFiles in response, got empty body or wrong number soknadFiles")
		} else {
			expectedFileIdStatusMap.forEach{ fileid, status ->  Assertions.assertTrue(files.filter {
				it.id == fileid && it.fileStatus == status &&
					((status == SoknadFile.FileStatus.ok && it.content != null) || (status != SoknadFile.FileStatus.ok && it.content == null) )}.firstOrNull() != null )}
		}

	}

	@Test
	fun `For two files responds with one file status ok and one deleted when this behaviour is set`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()
		val fileId2 = UUID.randomUUID().toString()

		// given
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.DELETED.name)
		fileFetchBehaviour.setFileResponseBehaviour(fileId2, FileResponses.One_MB.name)

		// when
		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId, fileId2), key)

		// then
		assertResponse(response = response,
			expectedFileIdStatusMap = mapOf(fileId to SoknadFile.FileStatus.deleted, fileId2 to SoknadFile.FileStatus.ok))
	}


	@Test
	fun `For two files responds with file status ok and file status not-found when this behaviour is set`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()
		val fileId2 = UUID.randomUUID().toString()

		// When
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.One_MB.name)
		fileFetchBehaviour.setFileResponseBehaviour(fileId2, FileResponses.NOT_FOUND.name)

		// Given
		val response = fillagerRestInterface.hentInnsendteFiler(listOf(fileId, fileId2), key)

		// Then
		assertResponse(response = response,
			expectedFileIdStatusMap = mapOf(fileId2 to SoknadFile.FileStatus.notfound, fileId to SoknadFile.FileStatus.ok))

	}


	@Test
	fun `Responds with file status deleted the first two calls then ok`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()

		// When
		val noOfFailedAttempts = 2
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.DELETED.name, noOfFailedAttempts)

		// Given
		val response1 = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)
		val response2 = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)
		val response3 = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)

		// Then
		assertResponse(response = response1,expectedFileIdStatusMap = mapOf(fileId to SoknadFile.FileStatus.deleted))
		assertResponse(response = response2,expectedFileIdStatusMap = mapOf(fileId to SoknadFile.FileStatus.deleted))
		assertResponse(response = response3,expectedFileIdStatusMap = mapOf(fileId to SoknadFile.FileStatus.ok))

	}

	@Test
	fun `Responds with file status not-found the first two calls then ok`() {

		val key = UUID.randomUUID().toString()
		val fileId = UUID.randomUUID().toString()

		// When
		val noOfFailedAttempts: Int = 2
		fileFetchBehaviour.setFileResponseBehaviour(fileId, FileResponses.NOT_FOUND.name, noOfFailedAttempts)

		// Given
		val response1 = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)
		val response2 = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)
		val response3 = fillagerRestInterface.hentInnsendteFiler(listOf(fileId), key)

		// Then
		assertResponse(response = response1,expectedFileIdStatusMap = mapOf(fileId to SoknadFile.FileStatus.notfound))
		assertResponse(response = response2,expectedFileIdStatusMap = mapOf(fileId to SoknadFile.FileStatus.notfound))
		assertResponse(response = response3,expectedFileIdStatusMap = mapOf(fileId to SoknadFile.FileStatus.ok))
	}

}
