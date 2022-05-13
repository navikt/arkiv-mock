package no.nav.soknad.arkivering.arkivmock.config

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@EnableJwtTokenValidation(ignore = [
	"org.springframework",
	"no.nav.soknad.arkivering.arkivmock.rest.HealthCheck",
	"io.swagger",
	"org.springdoc",
	"org.webjars.swagger-ui"
])
@Profile("dev-disabled-until-we-find-joark")
@Configuration
class JwtTokenValidationConfig
