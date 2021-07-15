package no.nav.soknad.arkivering.arkivmock.db

import no.nav.soknad.arkivering.arkivmock.ApplicationState

interface RenewService {
	fun startRenewTasks(applicationState: ApplicationState)
}
