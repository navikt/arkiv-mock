package no.nav.soknad.arkivering.arkivmock.config

import com.natpryce.konfig.*
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import no.nav.soknad.arkivering.arkivmock.ApplicationState
import no.nav.soknad.arkivering.arkivmock.db.*
import org.springframework.context.annotation.Bean
import java.io.File

private val defaultProperties = ConfigurationMap(
	mapOf(
		"KAFKA_BOOTSTRAP_SERVERS" to "localhost:9092",
		"NUMBER_OF_CALLS_TOPIC" to "privat-soknadInnsendt-endToEndTests-numberOfCalls",
		"ENTITIES_TOPIC" to "privat-soknadInnsendt-endToEndTests-entities",

		"KAFKA_USERNAME" to "arkiv-mock",
		"KAFKA_PASSWORD" to "",
		"KAFKA_SECURITY" to "",
		"KAFKA_SECPROT" to "",
		"KAFKA_SASLMEC" to "",

		"DATABASE_HOST" to "localhost",
		"DATABASE_PORT" to "5432",
		"DATABASE_NAME" to "arkivmock",
		"VAULT_DB_PATH" to "",
		"DATABASE_JDBC_URL" to "",

		"APPLICATION_PROFILE" to "spring",
	)
)

val appConfig =
	EnvironmentVariables() overriding
		systemProperties() overriding
		ConfigurationProperties.fromResource(Configuration::class.java, "/application.yml") overriding
		defaultProperties

private fun String.configProperty(): String = appConfig[Key(this, stringType)]

fun readFileAsText(fileName: String, default: String) = try { File(fileName).readText(Charsets.UTF_8) } catch (e: Exception ) { default }

data class AppConfiguration(val kafkaConfig: KafkaConfig = KafkaConfig(), val dbConfig: DBConfig = DBConfig()) {
	val applicationState = ApplicationState()

	data class KafkaConfig(
		val username: String = readFileAsText("/var/run/secrets/nais.io/serviceuser/username", "KAFKA_USERNAME".configProperty()),
		val password: String = readFileAsText("/var/run/secrets/nais.io/serviceuser/password", "KAFKA_PASSWORD".configProperty()),
		val servers: String = readFileAsText("/var/run/secrets/nais.io/kv/kafkaBootstrapServers", "KAFKA_BOOTSTRAP_SERVERS".configProperty()),
		val secure: String = "KAFKA_SECURITY".configProperty(),
		val protocol: String = "KAFKA_SECPROT".configProperty(), // SASL_PLAINTEXT | SASL_SSL
		val salsmec: String = "KAFKA_SASLMEC".configProperty(), // PLAIN
		val saslJaasConfig: String = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";",

		val numberOfCallsTopic: String = "NUMBER_OF_CALLS_TOPIC".configProperty(),
		val entitiesTopic: String = "ENTITIES_TOPIC".configProperty(),
	)

	data class DBConfig(
		val profiles: String = "APPLICATION_PROFILE".configProperty(),
		val databaseName: String = "DATABASE_NAME".configProperty(),
		val mountPathVault: String = "VAULT_DB_PATH".configProperty(),
		val url: String = "DATABASE_JDBC_URL".configProperty().ifBlank { null } ?: String.format(
			"jdbc:postgresql://%s:%s/%s",
			requireNotNull("DATABASE_HOST".configProperty()) { "database host must be set if jdbc url is not provided" },
			requireNotNull("DATABASE_PORT".configProperty()) { "database port must be set if jdbc url is not provided" },
			requireNotNull("DATABASE_NAME".configProperty()) { "database name must be set if jdbc url is not provided" }),
		val embedded: Boolean = profiles == "spring",
		val useVault: Boolean = profiles == "dev",
		val credentialService: CredentialService = if (useVault) VaultCredentialService() else EmbeddedCredentialService(),
		val renewService: RenewService = if (useVault) RenewVaultService(credentialService) else EmbeddedRenewService(credentialService)
	)
}

@org.springframework.context.annotation.Configuration
class ConfigConfig {
	@Bean
	fun appConfiguration() = AppConfiguration()
}
