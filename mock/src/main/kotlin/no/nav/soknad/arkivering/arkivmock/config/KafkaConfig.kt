package no.nav.soknad.arkivering.arkivmock.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kafkaconfig")
class KafkaConfig {
	lateinit var secure: String
	lateinit var kafkaBrokers: String
	lateinit var truststorePath: String
	lateinit var keystorePath: String
	lateinit var credstorePassword: String

	lateinit var entitiesTopic: String
	lateinit var numberOfCallsTopic: String
}
