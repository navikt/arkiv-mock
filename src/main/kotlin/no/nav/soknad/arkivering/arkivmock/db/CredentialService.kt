package no.nav.soknad.arkivering.arkivmock.db

import com.zaxxer.hikari.HikariDataSource
import no.nav.soknad.arkivering.arkivmock.ApplicationState

interface CredentialService {

	suspend fun runRenewCredentialsTask(applicationState: ApplicationState)

	fun getNewCredentials(mountPath: String, databaseName: String, role: Role): Credentials

	fun renewCredentialsTaskData(): RenewCredentialsTaskData?

	fun setRenewCredentialsTaskData(
		dataSource: HikariDataSource,
		mountPath: String,
		databaseName: String,
		role: Role
	)
}
