package no.digdir.efmesindexreader.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClearScrollDTO {
    boolean succeeded;
    @JsonProperty("num_freed")
    long numFreed;
}
