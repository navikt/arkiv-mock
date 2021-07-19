package no.nav.soknad.arkivering.arkivmock.config

import com.zaxxer.hikari.HikariDataSource
import no.nav.soknad.arkivering.arkivmock.db.Database
import no.nav.soknad.arkivering.arkivmock.db.EmbeddedDatabase
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JpaConfig(private val appConfig: AppConfiguration) {

	private val logger = LoggerFactory.getLogger(javaClass)

	@Bean
	fun getDataSource() = initDatasource()

	private fun initDatasource(): HikariDataSource {
		val database = if (appConfig.dbConfig.embedded) {
			EmbeddedDatabase(appConfig.dbConfig, appConfig.dbConfig.credentialService)
		} else {
			Database(appConfig.dbConfig, appConfig.dbConfig.credentialService)
		}
 		appConfig.dbConfig.renewService.startRenewTasks(appConfig.applicationState)

		appConfig.applicationState.ready = true
		appConfig.applicationState.alive = true
		logger.info("Datasource is initialised")
		return database.dataSource
	}
}
