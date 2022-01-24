package no.nav.soknad.arkivering.arkivmock.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.soknad.arkivering.arkivmock.dto.OpprettJournalpostResponse
import no.nav.soknad.arkivering.arkivmock.exceptions.InternalServerErrorException
import no.nav.soknad.arkivering.arkivmock.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BehaviourService(val objectMapper: ObjectMapper) {
	private val logger = LoggerFactory.getLogger(javaClass)

	private val behaviours: MutableMap<String, BehaviourDto> = mutableMapOf()

	fun setNormalBehaviour(key: String) {
		val calls = behaviours[key]?.calls ?: 0
		behaviours[key] = BehaviourDto(BEHAVIOUR.NORMAL, null, -1, calls)
	}

	fun mockException(key: String, statusCode: Int, forAttempts: Int) {
		val calls = behaviours[key]?.calls ?: 0

		val mockedException = when (statusCode) {
			404  -> NotFoundException()
			500  -> InternalServerErrorException()
			else -> RuntimeException("Not implemented")
		}

		behaviours[key] = BehaviourDto(BEHAVIOUR.MOCK_EXCEPTION, mockedException, forAttempts, calls)
	}

	fun mockResponseWithErroneousBody(key: String, forAttempts: Int) {
		if (!behaviours.containsKey(key)) {
			setNormalBehaviour(key)
		}
		behaviours[key]!!.behaviour = BEHAVIOUR.RESPOND_WITH_ERRONEOUS_BODY
		behaviours[key]!!.forAttempts = forAttempts
	}

	fun reactToArchiveRequest(key: String) {
		if (!behaviours.containsKey(key)) {
			logger.warn("$key: have not registered a behaviour for key. Will proceed as normal.")
			return
		}
		val behaviour = behaviours[key]!!
		behaviour.calls++
		if (behaviour.behaviour == BEHAVIOUR.MOCK_EXCEPTION && behaviour.calls <= behaviour.forAttempts && behaviour.mockedException != null) {
			logger.info("$key: will return exception for key.")
			throw behaviour.mockedException as Exception
		}
	}

	fun alterResponse(key: String, response: OpprettJournalpostResponse): String? {
		val jsonResponse = objectMapper.writeValueAsString(response)
		if (!behaviours.containsKey(key)) {
			logger.warn("$key: have not registered a behaviour for key. Will return normal response.")
			return jsonResponse
		}

		val behaviour = behaviours[key]!!
		return if (behaviour.behaviour == BEHAVIOUR.RESPOND_WITH_ERRONEOUS_BODY && behaviour.calls <= behaviour.forAttempts) {
			logger.info("$key: will return erroneous response.")
			"THIS_IS_A_MOCKED_INVALID_RESPONSE"
		} else {
			logger.info("$key: will return normal response")
			jsonResponse
		}
	}

	fun getNumberOfCallsThatHaveBeenMade(key: String) = behaviours[key]?.calls ?: -1
}

data class BehaviourDto(
	var behaviour: BEHAVIOUR = BEHAVIOUR.NORMAL,
	var mockedException: Exception? = null,
	var forAttempts: Int = -1,
	var calls: Int = 0
)

enum class BEHAVIOUR { NORMAL, MOCK_EXCEPTION, RESPOND_WITH_ERRONEOUS_BODY }
