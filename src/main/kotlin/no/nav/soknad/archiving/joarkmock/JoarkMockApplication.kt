package no.nav.soknad.archiving.joarkmock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JoarkMockApplication

fun main(args: Array<String>) {
	runApplication<JoarkMockApplication>(*args)
}
