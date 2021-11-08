package no.nav.soknad.arkivering.arkivmock.config

import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

	override fun configure(http: HttpSecurity) {
		http
			.csrf().disable()
			.authorizeRequests()
			.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
			.antMatchers(HttpMethod.POST, "/login", "/register").permitAll()
			.antMatchers(HttpMethod.GET, "/internal").permitAll()
			.antMatchers(HttpMethod.DELETE, "/rest/journalpostapi/v1/reset").permitAll()
			.antMatchers("/rest/journalpostapi/v1/journalpost").permitAll()
			.and()
			.httpBasic()
			.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	}
}
