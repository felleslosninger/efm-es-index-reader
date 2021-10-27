package no.digdir.efmesindexreader.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.common.oauth.JwtTokenClient;
import no.difi.move.common.oauth.JwtTokenConfig;
import no.difi.move.common.oauth.JwtWebClient;
import no.digdir.efmesindexreader.config.EsIndexReaderProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

@Configuration
@EnableRetry
@EnableConfigurationProperties(EsIndexReaderProperties.class)
@Slf4j
public class LoggingProxyWebClientConfiguration {

    @SneakyThrows
    @Bean
    public JwtTokenClient jwtTokenClient(EsIndexReaderProperties props) {
        JwtTokenConfig config = new JwtTokenConfig(
                props.getOidc().getClientId(),
                props.getOidc().getUrl().toString(),
                props.getOidc().getAudience(),
                Collections.singletonList(props.getOidc().getScopes()),
                props.getOidc().getKeystore()
        );

        return new JwtTokenClient(config);
    }

    @Bean(name = "LoggingProxyWebClient")
    WebClient loggingProxyWebClient(EsIndexReaderProperties properties, JwtTokenClient jwtTokenClient) {
        return JwtWebClient.create(getLoggingProxyURI(properties).toString(), properties.getOidc().getRegistrationId(), jwtTokenClient);
    }

    @SneakyThrows(URISyntaxException.class)
    public URI getLoggingProxyURI(EsIndexReaderProperties properties) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(properties.getLoggingProxy().getEndpointURL().toURI())
                .path("api");

        URI uri = builder.build().toUri();
        log.trace("Built logging proxy URL: {}", uri);
        return uri;
    }

}
