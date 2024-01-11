package no.nav.soknad.arkivering.arkivmock.service

import no.nav.soknad.innsending.model.SoknadFile
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.OffsetDateTime

@Service
class FileMockService {


	private val fileBehaviours: MutableMap<String, FileResponse_Behaviour> = mutableMapOf()

	fun setFileResponse(key: String, response: String, forAttempts: Int) {
		fileBehaviours.put(key, FileResponse_Behaviour(behaviour = FileResponses.valueOf(response), forAttempts = forAttempts))
	}

	fun getFileResponse(key: String): FileResponse_Behaviour {
		return fileBehaviours.getOrDefault(key, FileResponse_Behaviour(FileResponses.One_MB, mockedException = null, forAttempts = -1, calls = 0 ))
	}

	fun getFile(filId: String): SoknadFile {
		val response = getFileResponse(filId)
		response.calls += 1
		fileBehaviours.put(filId, response)

		// For henting av en fil kan det være satt opp at responsen skal gi feil, enten not-found eller deleted, x antall attempt før default respons.
		// Hvis antall kall utført for å hente fil er mindre enn antall attempts spesifisert skal deleted eller not-found respons returneres.
		if (response.forAttempts >= response.calls) {
			return SoknadFile(
				fileStatus = if (response.behaviour == FileResponses.DELETED) SoknadFile.FileStatus.deleted else SoknadFile.FileStatus.notfound, id = filId,
				content = null, createdAt = null)
		}

		// Hvis det er satt opp at det skal være noen feil attempts før ok respons, og dette antallet er nådd, sett default fil respons
		if (response.forAttempts > -1 )
			response.behaviour = FileResponses.One_MB

		// Returner iht. konfigureringen i response objektet.
		return when (response.behaviour) {
			FileResponses.DELETED -> {
				SoknadFile(fileStatus = SoknadFile.FileStatus.deleted, id = filId, content = null, createdAt = OffsetDateTime.now().minusHours(1L))
			}
			FileResponses.NOT_FOUND -> {
				SoknadFile(fileStatus = SoknadFile.FileStatus.notfound, id = filId, content = null, createdAt = OffsetDateTime.now().minusHours(1L))
			}
			else -> {
				SoknadFile(
					fileStatus =  SoknadFile.FileStatus.ok,
					id = filId,
					content = getBytesFromFile("/files/${response.behaviour.name}.pdf"),
					createdAt = OffsetDateTime.now().minusMinutes(1L)
				)
			}
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
