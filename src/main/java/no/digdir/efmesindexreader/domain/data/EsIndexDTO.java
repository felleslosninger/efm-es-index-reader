package no.digdir.efmesindexreader.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EsIndexDTO {
    @JsonProperty("_scroll_id")
    private String scrollId;
    private Hits hits;
}
