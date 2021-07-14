package no.digdir.efmesindexreader.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URL;

@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "digdir")
@Data
public class EsIndexReaderProperties {

    @Valid
    private ElasticsearchProperties elasticsearch;

    @Valid
    private LoggingProxyProperties loggingProxy;

    @NotNull
    public long readTimeoutInMs;
    @NotNull
    public Integer connectTimeoutInMs;
    @NotNull
    public long writeTimeoutInMs;
    @Data
    public static class ElasticsearchProperties {
        @NotNull
        private URL endpointURL;
    }

    @Data
    public static class LoggingProxyProperties {
        @NotNull
        private URL endpointURL;
    }
}
