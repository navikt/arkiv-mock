package no.nav.soknad.arkivering.arkivmock.config

import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
class WebSecurityConfig {

	@Bean
	fun filterChain(http: HttpSecurity): SecurityFilterChain {
		http
			.csrf().disable()
			.authorizeHttpRequests()
			.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
			.requestMatchers("/rest/journalpostapi/v1/journalpost").permitAll()
			.and()
			.httpBasic()
			.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		return http.build()
	}
}
