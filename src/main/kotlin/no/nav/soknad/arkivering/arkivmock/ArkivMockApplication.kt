package no.nav.soknad.arkivering.arkivmock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ArkivMockApplication

fun main(args: Array<String>) {
	runApplication<ArkivMockApplication>(*args)
}
