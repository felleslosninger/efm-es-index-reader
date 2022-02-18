package no.digdir.efmesindexreader.service;

import no.digdir.efmesindexreader.EfmEsIndexReaderApplication;
import no.digdir.efmesindexreader.domain.data.EsIndexDTO;
import no.digdir.efmesindexreader.handler.EsIndexHandler;
import no.digdir.efmesindexreader.router.EsIndexRouter;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(EsIndexRouter.class)
//@AutoConfigureWebFlux
@ContextConfiguration(classes = EfmEsIndexReaderApplication.class)
@WebFluxTest(controllers = EsIndexRouter.class,excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class})
@TestPropertySource("classpath:/config/application-test.properties")
@ActiveProfiles("test")
public class ElasticsearchWebClientTest {

    private static MockWebServer mockWebServer;

//    @Autowired
//    private EsIndexReaderProperties properties;


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

    @BeforeAll
    public static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    public void initialize() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockWebServer.shutdown();

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
