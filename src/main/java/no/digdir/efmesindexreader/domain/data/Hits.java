package no.digdir.efmesindexreader.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Hits {
    private Long total;
    @JsonProperty("hits")
    private List<HitDTO> hitDtoList;
}