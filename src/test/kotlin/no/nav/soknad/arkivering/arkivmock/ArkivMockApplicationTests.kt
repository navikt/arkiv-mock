package no.nav.soknad.arkivering.arkivmock

import com.ninjasquad.springmockk.MockkBean
import no.nav.soknad.arkivering.arkivmock.service.kafka.KafkaPublisher
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ArkivMockApplicationTests {

	@Suppress("unused")
	@MockkBean(relaxed = true)
	private lateinit var kafkaPublisher: KafkaPublisher

	@Test
	fun `Spring context loads`() {
	}
}
