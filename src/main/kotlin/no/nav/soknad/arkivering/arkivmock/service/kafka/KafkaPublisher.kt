package no.nav.soknad.arkivering.arkivmock.service.kafka

import no.nav.soknad.arkivering.arkivmock.config.AppConfiguration
import no.nav.soknad.arkivering.arkivmock.dto.ArchiveEntity
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class KafkaPublisher(private val appConfiguration: AppConfiguration) {
	private val logger = LoggerFactory.getLogger(javaClass)

	private val kafkaStringProducer = KafkaProducer<String, String>(kafkaConfigMap(StringSerializer()))
	private val kafkaIntProducer = KafkaProducer<String, Int>(kafkaConfigMap(IntegerSerializer()))


	fun putDataOnTopic(key: String, value: ArchiveEntity, headers: Headers = RecordHeaders()) {
		val topic = appConfiguration.kafkaConfig.entitiesTopic
		val kafkaProducer = kafkaStringProducer
		putDataOnTopic(key, value.toString(), headers, topic, kafkaProducer)
	}

	fun putNumberOfCallsOnTopic(key: String, value: Int, headers: Headers = RecordHeaders()) {
		val topic = appConfiguration.kafkaConfig.numberOfCallsTopic
		val kafkaProducer = kafkaIntProducer
		putDataOnTopic(key, value, headers, topic, kafkaProducer)
	}

	private fun <T> putDataOnTopic(
		key: String?, value: T, headers: Headers, topic: String,
		kafkaProducer: KafkaProducer<String, T>
	): RecordMetadata {

		val producerRecord = ProducerRecord(topic, key, value)
		headers.add("MESSAGE_ID", UUID.randomUUID().toString().toByteArray())
		headers.forEach { h -> producerRecord.headers().add(h) }

		logger.info("$key: Publishing to topic '$topic' with key: '$key', value: '$value'")

		return kafkaProducer
			.send(producerRecord)
			.get(1000, TimeUnit.MILLISECONDS) // Blocking call
	}


	private fun <T> kafkaConfigMap(valueSerializer: Serializer<T>): MutableMap<String, Any> {
		return HashMap<String, Any>().also {
			it[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = appConfiguration.kafkaConfig.servers
			it[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
			it[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = valueSerializer::class.java
			if (appConfiguration.kafkaConfig.secure == "TRUE") {
				it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = appConfiguration.kafkaConfig.protocol
				it[SaslConfigs.SASL_JAAS_CONFIG] = appConfiguration.kafkaConfig.saslJaasConfig
				it[SaslConfigs.SASL_MECHANISM] = appConfiguration.kafkaConfig.salsmec
			}
		}
	}
}
