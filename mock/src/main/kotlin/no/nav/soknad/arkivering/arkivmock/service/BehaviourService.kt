package no.nav.soknad.arkivering.arkivmock.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.soknad.arkivering.arkivmock.dto.OpprettJournalpostResponse
import no.nav.soknad.arkivering.arkivmock.exceptions.InternalServerErrorException
import no.nav.soknad.arkivering.arkivmock.exceptions.NotFoundException
import no.nav.soknad.arkivering.arkivmock.service.BEHAVIOUR.*
import no.nav.soknad.innsending.model.SoknadFile
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.OffsetDateTime

@Service
class BehaviourService(val objectMapper: ObjectMapper) {
	private val logger = LoggerFactory.getLogger(javaClass)

	private val behaviours: MutableMap<String, BehaviourDto> = mutableMapOf()
	private val fileBehaviours: MutableMap<String, FileResponse_Behaviour> = mutableMapOf()

	fun setNormalBehaviour(key: String) {
		val calls = behaviours[key]?.calls ?: 0
		behaviours[key] = BehaviourDto(NORMAL, null, -1, calls)
	}

	fun mockException(key: String, statusCode: Int, forAttempts: Int) {
		val calls = behaviours[key]?.calls ?: 0

		val mockedException = when (statusCode) {
			404  -> NotFoundException()
			500  -> InternalServerErrorException()
			else -> RuntimeException("Not implemented")
		}

		behaviours[key] = BehaviourDto(MOCK_EXCEPTION, mockedException, forAttempts, calls)
	}

	fun mockResponseWithErroneousBody(key: String, forAttempts: Int) {
		if (!behaviours.containsKey(key)) {
			setNormalBehaviour(key)
		}
		behaviours[key]!!.behaviour = RESPOND_WITH_ERRONEOUS_BODY
		behaviours[key]!!.forAttempts = forAttempts
	}

	fun reactToArchiveRequest(key: String) {
		if (!behaviours.containsKey(key)) {
			logger.warn("$key: has not registered a behaviour for key. Will proceed as normal.")
			return
		}
		val behaviour = behaviours[key]!!
		behaviour.calls++
		if (behaviour.behaviour == MOCK_EXCEPTION && behaviour.calls <= behaviour.forAttempts && behaviour.mockedException != null) {
			logger.info("$key: will return exception for key.")
			throw behaviour.mockedException as Exception
		}
	}

	fun alterResponse(key: String, response: OpprettJournalpostResponse): String? {
		val jsonResponse = objectMapper.writeValueAsString(response)
		val journalpostIdMessage = "Will return normal response with journalpostId '${response.journalpostId}'"

		if (!behaviours.containsKey(key)) {
			logger.warn("$key: has not registered a behaviour for key. $journalpostIdMessage")
			return jsonResponse
		}

		val behaviour = behaviours[key]!!
		return if (behaviour.behaviour == RESPOND_WITH_ERRONEOUS_BODY && behaviour.calls <= behaviour.forAttempts) {
			logger.info("$key: will return erroneous response.")
			"THIS_IS_A_MOCKED_INVALID_RESPONSE"
		} else {
			logger.info("$key: $journalpostIdMessage")
			jsonResponse
		}
	}

	fun getNumberOfCallsThatHaveBeenMade(key: String) = behaviours[key]?.calls ?: -1

	fun setFileResponse(key: String, response: String) {
		fileBehaviours.put(key, FileResponse_Behaviour(behaviour = FileResponses.valueOf(response)))
	}

	fun getFileResponse(key: String): FileResponse_Behaviour {
		return fileBehaviours.getOrDefault(key, FileResponse_Behaviour(FileResponses.One_MB, mockedException = null, forAttempts = -1, calls = 0 ))
	}

	fun getFile(filId: String): SoknadFile {
		val response = getFileResponse(filId)
		response.calls += 1
		fileBehaviours.put(filId, response)

		return if (response.forAttempts > response.calls) {
			SoknadFile(fileStatus = SoknadFile.FileStatus.notfound, id = filId, content = null, createdAt = null)
		} else 	if (response.behaviour == FileResponses.DELETED) {
			SoknadFile(fileStatus = SoknadFile.FileStatus.deleted, id = filId, content = null, createdAt = OffsetDateTime.now().minusHours(1L))
		} else if (response.behaviour == FileResponses.NOT_FOUND) {
			SoknadFile(fileStatus = SoknadFile.FileStatus.deleted, id = filId, content = null, createdAt = OffsetDateTime.now().minusHours(1L))
		} else {
			SoknadFile(
				fileStatus =  SoknadFile.FileStatus.ok,
				id = filId,
				content = getBytesFromFile("/files/${response.behaviour.name}+.pdf"),
				createdAt = OffsetDateTime.now().minusMinutes(1L)
			)
		}

	}

	fun getBytesFromFile(path: String): ByteArray {
		val resourceAsStream = this::class.java.getResourceAsStream(path)
		val outputStream = ByteArrayOutputStream()
		resourceAsStream.use { input ->
			outputStream.use { output ->
				input!!.copyTo(output)
			}
		}
		return outputStream.toByteArray()
	}

}

data class BehaviourDto(
	var behaviour: BEHAVIOUR = NORMAL,
	var mockedException: Exception? = null,
	var forAttempts: Int = -1,
	var calls: Int = 0
)

enum class BEHAVIOUR { NORMAL, MOCK_EXCEPTION, RESPOND_WITH_ERRONEOUS_BODY }

enum class FileResponses {	OneHundred_KB, One_MB, Ten_MB,	Fifty_50_MB,	DELETED,	NOT_FOUND }

data class FileResponse_Behaviour(
	var behaviour: FileResponses = FileResponses.One_MB,
	var mockedException: Exception? = null,
	var forAttempts: Int = -1,
	var calls: Int = 0
)
