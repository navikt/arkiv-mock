package no.nav.soknad.archiving.joarkmock.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.soknad.archiving.dto.JoarkResponse
import no.nav.soknad.archiving.joarkmock.exceptions.InternalServerErrorException
import no.nav.soknad.archiving.joarkmock.exceptions.NotFoundException
import org.springframework.stereotype.Service

@Service
class BehaviourService(val objectMapper: ObjectMapper) {

	private var behaviour: BEHAVIOUR = BEHAVIOUR.NORMAL
	private var mockedException: Exception? = null
	private var forAttempts: Int = -1
	private var calls: Int = 0

	fun setNormalBehaviour() {
		behaviour = BEHAVIOUR.NORMAL
		mockedException = null
		forAttempts = -1
		calls = 0
	}

	fun mockException(statusCode: Int, forAttempts: Int) {
		this.forAttempts = forAttempts
		this.behaviour = BEHAVIOUR.MOCK_EXCEPTION

		this.mockedException = when (statusCode) {
			404 -> NotFoundException()
			500 -> InternalServerErrorException()
			else -> RuntimeException("Not implemented")
		}
	}

	fun mockResponseWithErroneousBody() {
		behaviour = BEHAVIOUR.RESPOND_WITH_ERRONEOUS_BODY
	}

	fun reactToArchiveRequest() {
		calls++
		if (behaviour == BEHAVIOUR.MOCK_EXCEPTION && calls <= forAttempts && mockedException != null)
			throw mockedException as Exception
	}

	fun alterResponse(response: JoarkResponse): String {
		return if (behaviour == BEHAVIOUR.NORMAL)
			objectMapper.writeValueAsString(response)
		else
			"THIS IS A MOCKED INVALID RESPONSE"
	}

	fun getNumberOfCallsThatHaveBeenMade() = calls
}

enum class BEHAVIOUR { NORMAL, MOCK_EXCEPTION, RESPOND_WITH_ERRONEOUS_BODY }
