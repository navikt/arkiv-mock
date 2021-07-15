package no.nav.soknad.arkivering.arkivmock.db

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.soknad.arkivering.arkivmock.ApplicationState
import org.slf4j.LoggerFactory

class EmbeddedRenewService(private val credentialService: CredentialService) : RenewService {
	private val log = LoggerFactory.getLogger(javaClass)

	override fun startRenewTasks(applicationState: ApplicationState) {
		log.info("renewTokenTask $applicationState")

		GlobalScope.launch {
			try {
				credentialService.runRenewCredentialsTask(applicationState)
			} catch (e: Exception) {
				log.error("Noe gikk galt ved fornying av vault-credentials", e.message)
			} finally {
				applicationState.ready = false
			}
		}
	}
}
