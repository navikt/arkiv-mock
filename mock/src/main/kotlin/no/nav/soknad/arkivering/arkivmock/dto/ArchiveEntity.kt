package no.nav.soknad.arkivering.arkivmock.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

/**
 * Subset of [ArkivData], used for broadcasting on Kafka that the app received data.
 */
class ArchiveEntity(
	val id: String,
	val title: String,
	val tema: String,
	val timesaved: Long
) {
	override fun toString(): String {
		val mapper = ObjectMapper()
		mapper.enable(SerializationFeature.INDENT_OUTPUT)
		mapper.findAndRegisterModules()
		return mapper.writeValueAsString(this)
	}
}
