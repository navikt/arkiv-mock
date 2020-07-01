package no.nav.soknad.archiving.joarkmock.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "joark")
class JoarkDbData(
	@Id
	val id: String,
	val message: String,
	val name: String
) {
	override fun toString(): String {
		val mapper = ObjectMapper()
		mapper.enable(SerializationFeature.INDENT_OUTPUT)
		return mapper.writeValueAsString(this)
	}
}
