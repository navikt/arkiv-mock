package no.nav.soknad.arkivering.arkivmock.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
open class WebSecurityConfig {

	@Bean
	open fun filterChain(http: HttpSecurity): SecurityFilterChain {
		http
			.csrf().disable()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		return http.build()
	}
}
