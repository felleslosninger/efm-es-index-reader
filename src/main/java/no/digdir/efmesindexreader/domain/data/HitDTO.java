package no.digdir.efmesindexreader.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class HitDTO {
    @JsonProperty("_index")
    private String index;
    @JsonProperty("_source")
    private ObjectNode source;
}
