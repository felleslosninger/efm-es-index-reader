package no.digdir.efmesindexreader.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.difi.move.common.config.KeystoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URL;

@NoArgsConstructor
@ConfigurationProperties(prefix = "digdir")
@Data
public class EsIndexReaderProperties {

    @Valid
    private ElasticsearchProperties elasticsearch;

    @Valid
    private LoggingProxyProperties loggingProxy;

    @Valid
    private Oidc oidc;

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
        @NotNull
        @NotEmpty
        private String schedulerCronExpr;
    }

    @Data
    public static class LoggingProxyProperties {
        @NotNull
        private URL endpointURL;
    }

    @Data
    public static class Oidc {
        @NotNull
        private String registrationId;
        @NotNull
        private String clientId;
        private URL url;
        @NestedConfigurationProperty
        private KeystoreProperties keystore;
        @NotNull
        private String audience;
        @NotNull
        private String scopes;
    }
}
