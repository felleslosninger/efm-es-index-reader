package no.digdir.efmesindexreader.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.common.oauth.JwtTokenClient;
import no.difi.move.common.oauth.JwtTokenConfig;
import no.difi.move.common.oauth.JwtWebClient;
import no.digdir.efmesindexreader.config.EsIndexReaderProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@EnableRetry
@Configuration
@EnableConfigurationProperties(EsIndexReaderProperties.class)
@Slf4j
public class WebClientConfiguration {

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
        return JwtWebClient.createWithReactorClientConnector(
                getLoggingProxyURI(properties).toString(), properties.getOidc().getRegistrationId(), jwtTokenClient, getReactorClientConnector());
    }

    @SneakyThrows(URISyntaxException.class)
    public URI getLoggingProxyURI(EsIndexReaderProperties properties) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(properties.getLoggingProxy().getEndpointURL().toURI())
                .path("api");

        URI uri = builder.build().toUri();
        log.trace("Built logging proxy URL: {}", uri);
        return uri;
    }

    @Bean(name = "EsWebClient")
    public static WebClient esWebClient(EsIndexReaderProperties properties) {
        return WebClient.builder()
                .exchangeStrategies(getExchangeStrategies())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(TcpClient
                        .create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.connectTimeoutInMs)
                        .doOnConnected(connection -> {
                            connection.addHandlerLast(new ReadTimeoutHandler(properties.readTimeoutInMs, TimeUnit.MILLISECONDS));
                            connection.addHandlerLast(new WriteTimeoutHandler(properties.writeTimeoutInMs, TimeUnit.MILLISECONDS));
                        }))))
                .build();
    }


    private static ReactorClientHttpConnector getReactorClientConnector () {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("connProvider")
                .maxConnections(100)
                .pendingAcquireMaxCount(100000)
                .build();
        return new ReactorClientHttpConnector(HttpClient.create(connectionProvider));
    }

    private static ExchangeStrategies getExchangeStrategies() {
        ObjectMapper objectMapper = getObjectMapper();
        return ExchangeStrategies.builder()
                .codecs(config -> {
                    config.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                    config.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                    config.defaultCodecs().maxInMemorySize(32 * 1024 * 1024);
                }).build();
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }
}
