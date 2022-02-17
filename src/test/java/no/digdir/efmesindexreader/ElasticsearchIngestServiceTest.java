package no.digdir.efmesindexreader;

import no.digdir.efmesindexreader.config.EsIndexReaderProperties;
import no.digdir.efmesindexreader.domain.data.EsIndexDTO;
import no.digdir.efmesindexreader.handler.EsIndexHandler;
import no.digdir.efmesindexreader.service.ElasticsearchIngestService;
import no.digdir.efmesindexreader.service.LoggingProxySender;
import no.digdir.efmesindexreader.service.WebClientConfiguration;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
//@WebFluxTest(controllers = EsIndexRouter.class,excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class})
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:/config/application-test.properties")
@ActiveProfiles("test")
public class ElasticsearchIngestServiceTest {
	private static MockWebServer mockWebServer;

	@Autowired
	private EsIndexReaderProperties properties;

	private ElasticsearchIngestService target;

	@Autowired
	private WebTestClient webTestClient;

	@MockBean(name = "EsWebClient")
	WebClient webClient;
	@MockBean
	private EsIndexHandler handler;

	@MockBean
	private LoggingProxySender loggingProxySender;

	@MockBean
	private WebClientConfiguration webClientConfiguration;

	URI esUri;

	URI scrollUri;

	URI deleteScrollUri;

	String scrollId = "UniqueScrollId";

	@BeforeAll
	public static void setUp() throws IOException {
		//mockWebServer = new MockWebServer();
		//mockWebServer.start();
	}

	@BeforeEach
	public void initialize() {
		MockitoAnnotations.openMocks(this);
		target = new ElasticsearchIngestService(webClient, properties);

		esUri = UriComponentsBuilder.fromUriString(properties.getElasticsearch().getEndpointURL() + "graylog_0/_search?scroll=1m&pretty").build().toUri();
		scrollUri = UriComponentsBuilder.fromUriString(properties.getElasticsearch().getEndpointURL() + "_search/scroll?scroll=1m&scroll_id=" + scrollId + "&pretty").build().toUri();
		deleteScrollUri = UriComponentsBuilder.fromUriString(properties.getElasticsearch().getEndpointURL() + "_search/scroll?scroll_id=" + scrollId + "&pretty").build().toUri();
	}

	@AfterAll
	public static void tearDown() throws IOException {
		//mockWebServer.shutdown();

	}

	@Test
	public void getScrollDownloadURI_shouldReturnURI() {
		URI uri = target.getScrollDownloadURI("graylog_0");
		Assertions.assertThat(uri.toString()).isEqualTo(esUri.toString());
	}

	@Test
	public void getNextScrollURI_shouldReturnURI() {
		URI uri = target.getNextScrollURI(scrollId);
		Assertions.assertThat(uri.toString()).isEqualTo(scrollUri.toString());
	}

	@Test
	public void getDeleteScrollID_shouldReturnURI() {
		URI uri = target.getDeleteScrollURI(scrollId);
		Assertions.assertThat(uri.toString()).isEqualTo(deleteScrollUri.toString());
	}

	@Test
	public void openScrollIndex_shouldOpenScrollSuccessfully() {
		//webTestClient.get().uri("/esindex/?index=graylog_0")
		webTestClient.get().uri("/esindex/all")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(EsIndexDTO.class)
				.consumeWith(response -> Assertions.assertThat(response.getResponseBody()).isNotNull());

	}
}
