package no.nav.soknad.arkivering.arkivmock

import no.nav.soknad.arkivering.arkivmock.service.kafka.KafkaPublisher
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
class ArkivMockApplicationTests {

	@Suppress("unused")
	@MockBean
	private lateinit var kafkaPublisher: KafkaPublisher

	@Test
	fun `Spring context loads`() {
	}
}
