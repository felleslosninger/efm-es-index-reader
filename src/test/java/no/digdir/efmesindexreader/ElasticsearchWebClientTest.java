package no.digdir.efmesindexreader;

import no.digdir.efmesindexreader.config.EsIndexReaderProperties;
import no.digdir.efmesindexreader.service.ElasticsearchWebClient;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class ElasticsearchWebClientTest {
	//test med å bruke springrunner i greie i staden for og så autowired i staden for injectedmocks, mockbean i staden for mock. går også å bruke spy til.
	private static MockWebServer mockWebServer;

	@MockBean
	private EsIndexReaderProperties properties;

	@MockBean
	private ElasticsearchWebClient target;

	URI esUri;

	@BeforeAll
	public static void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
	}

	@BeforeEach
	public void initialize() {
		MockitoAnnotations.openMocks(this);
		esUri = UriComponentsBuilder.fromUriString(properties.getElasticsearch().getEndpointURL() + "graylog_0/_search?scroll=1m&pretty").build().toUri();
		when(target.getScrollDownloadURI("graylog_0")).thenReturn(esUri);
	}

	@AfterAll
	public static void tearDown() throws IOException {
		mockWebServer.shutdown();

	}

	@Test
	public void getScrollDownloadURI_shouldReturnURI() {
		URI uri = target.getScrollDownloadURI("graylog_0");
		Assertions.assertThat(uri.toString()).isEqualTo(esUri.toString());
	}

	@Test
	public void openScrollIndex_shouldOpenScrollSuccessfully() {

	}
}
