package no.nav.soknad.arkivering.arkivmock.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Subset of [ArkivData], used for broadcasting on Kafka that the app received data.
 */
class ArchiveEntity(
	val id: String,
	val title: String,
	val tema: String,
	val kanal: String,
	val timesaved: Long,
	val overstyrInnsynsregler: String? = null,
	@get:JsonInclude(JsonInclude.Include.NON_NULL)
	val dokumenter: List<ArchiveDocument>? = null
) {
	override fun toString(): String {
		val mapper = ObjectMapper()
		mapper.enable(SerializationFeature.INDENT_OUTPUT)
		mapper.findAndRegisterModules()
		return mapper.writeValueAsString(this)
	}
}

data class ArchiveDocument(
	val tittel: String,
	val brevkode: String?,
	val variantformater: List<String>,
	val antallSider: Int? = null
)
