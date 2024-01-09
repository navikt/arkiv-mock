package no.nav.soknad.arkivering.arkivmock.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.soknad.arkivering.arkivmock.dto.OpprettJournalpostResponse
import no.nav.soknad.arkivering.arkivmock.exceptions.*
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

	fun setNormalBehaviour(key: String) {
		val calls = behaviours[key]?.calls ?: 0
		behaviours[key] = BehaviourDto(NORMAL, null, -1, calls)
	}

	fun setDelayBehaviour(key: String) {
		val calls = behaviours[key]?.calls ?: 0
		behaviours[key] = BehaviourDto(DELAYED_NORMAL, null, 3, calls)
	}

	fun mockException(key: String, statusCode: Int, forAttempts: Int) {
		val calls = behaviours[key]?.calls ?: 0

		val mockedException = when (statusCode) {
			404  -> NotFoundException()
			408 ->  TimeoutException()
			409 ->  ConflictException()
			410 ->  GoneException()
			500  -> InternalServerErrorException()
			else -> RuntimeException("Not implemented $statusCode")
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
		} else if (behaviour.behaviour == DELAYED_NORMAL) {
			logger.info("$key: delay response.")
			if (behaviour.calls < behaviour.forAttempts)
				Thread.sleep(4*60*1002L)
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
}

data class BehaviourDto(
	var behaviour: BEHAVIOUR = NORMAL,
	var mockedException: Exception? = null,
	var forAttempts: Int = -1,
	var calls: Int = 0
)

enum class BEHAVIOUR { NORMAL, MOCK_EXCEPTION, RESPOND_WITH_ERRONEOUS_BODY, DELAYED_NORMAL }

enum class FileResponses {	OneHundred_KB, One_MB, Ten_MB,	Fifty_50_MB,	DELETED,	NOT_FOUND }
enum class SafResponses {	OK,	NOT_FOUND }

data class FileResponse_Behaviour(
	var behaviour: FileResponses = FileResponses.One_MB,
	var mockedException: Exception? = null,
	var forAttempts: Int = -1,
	var calls: Int = 0
)


data class SafResponse_Behaviour(
	var behaviour: SafResponses = SafResponses.NOT_FOUND,
	var mockedException: Exception? = null,
	var forAttempts: Int = -1,
	var calls: Int = 0
)
