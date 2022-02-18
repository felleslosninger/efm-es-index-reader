package no.digdir.efmesindexreader.service;

import no.digdir.efmesindexreader.handler.EsIndexHandler;
import no.digdir.efmesindexreader.router.EsIndexRouter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class})
@ContextConfiguration(classes = {EsIndexRouter.class, EsIndexHandler.class})
public class ElasticsearchWebClientTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private LoggingProxySender loggingProxySender;

    @MockBean
    private ElasticsearchIngestService elasticsearchIngestService;

    @Test
    public void openScrollIndex_shouldOpenScrollSuccessfully() {
        webTestClient.get().uri("/esindex/all")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(r -> Assertions.assertEquals("i'm a placeholder for a list of collected indexes which requires a DB", new String(r.getResponseBody())));

    }
}
