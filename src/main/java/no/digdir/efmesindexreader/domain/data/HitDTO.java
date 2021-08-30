package no.digdir.efmesindexreader.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HitDTO {
    @JsonProperty("_index")
    private String index;
    @JsonProperty("_source")
    private SourceDTO source;
}
