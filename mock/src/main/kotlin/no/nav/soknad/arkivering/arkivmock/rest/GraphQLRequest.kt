package no.nav.soknad.arkivering.arkivmock.rest

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


class GraphQLRequest(
	@param:JsonProperty("query") private val query: String,
	@param:JsonProperty("operationName") private val operationName: String,
	@param:JsonProperty("variables") private val variables: Map<String, Any>
) {

fun variables(): Map<String, Any> = variables


}
