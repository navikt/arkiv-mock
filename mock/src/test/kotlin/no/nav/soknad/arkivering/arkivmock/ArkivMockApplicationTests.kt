package no.nav.soknad.arkivering.arkivmock

import com.ninjasquad.springmockk.MockkBean
import no.nav.soknad.arkivering.arkivmock.service.kafka.KafkaPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ArkivMockApplicationTests {
	@LocalServerPort
	private var port: Int = 0

	@Suppress("unused")
	@MockkBean(relaxed = true)
	private lateinit var kafkaPublisher: KafkaPublisher

	@Test
	fun `Spring context loads`() {
		HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build().use { client ->
			val request = HttpRequest.newBuilder(URI("http://localhost:$port/health/status"))
				.timeout(Duration.ofSeconds(10))
				.GET()
				.build()
			val response = client.send(request, HttpResponse.BodyHandlers.ofString())
			assertEquals(200, response.statusCode())
			assertTrue(response.headers().allValues("Set-Cookie").none { it.contains("JSESSIONID") })
			val postRequest = HttpRequest.newBuilder(request.uri())
				.timeout(Duration.ofSeconds(10))
				.POST(HttpRequest.BodyPublishers.noBody())
				.build()
			assertEquals(405, client.send(postRequest, HttpResponse.BodyHandlers.discarding()).statusCode())
		}
	}
}
