package no.nav.soknad.arkivering.arkivmock.config

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder

@Configuration
class JacksonConfig {
	@Bean
	fun jsonMapperCustomizer(): JsonMapperBuilderCustomizer =
		JsonMapperBuilderCustomizer { it.accessorNaming(accessorNaming()) }
}

fun jsonMapperBuilder(): JsonMapper.Builder = jacksonMapperBuilder().accessorNaming(accessorNaming())

private fun accessorNaming() = DefaultAccessorNamingStrategy.Provider().withFirstCharAcceptance(true, true)
