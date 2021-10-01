package no.digdir.efmesindexreader.service;

import no.digdir.efmesindexreader.domain.data.HitDTO;
import no.digdir.efmesindexreader.domain.data.SourceDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource("classpath:/config/application-test.properties")
@ActiveProfiles("test")
public class ElasticsearchIngestServiceTest {

    @InjectMocks
    ElasticsearchIngestService service;

    private List<HitDTO> hitDTOList = new ArrayList<>();

    private SourceDTO sourceDTO1 = new SourceDTO();
    private SourceDTO sourceDTO2 = new SourceDTO();
    private SourceDTO sourceDTO3 = new SourceDTO();
    private SourceDTO sourceDTO4 = new SourceDTO();

    private HitDTO hitDTO1 = new HitDTO();
    private HitDTO hitDTO2 = new HitDTO();
    private HitDTO hitDTO3 = new HitDTO();
    private HitDTO hitDTO4 = new HitDTO();

    private int hitSize = 0;

    @Before
    public void setup() {
        sourceDTO1.setStatus("SENDT");
        sourceDTO2.setStatus("AAPNING");
        sourceDTO3.setStatus("POPPET");
        sourceDTO4.setStatus("OPPRETTET");

        hitDTO1.setSource(sourceDTO1);
        hitDTO2.setSource(sourceDTO2);
        hitDTO3.setSource(sourceDTO3);
        hitDTO4.setSource(sourceDTO4);

        hitDTOList.add(hitDTO1);
        hitDTOList.add(hitDTO2);
        hitDTOList.add(hitDTO3);
        hitDTOList.add(hitDTO4);
        hitSize = hitDTOList.size();

    }

    @Test
    public void filterOldStatusAndPutInFluxTest_shouldFilter() {
        List<HitDTO> filteredResult = new ArrayList<>();
        Flux<HitDTO> hitDTOFlux = Flux.create(fluxSink -> {
            service.filterOldStatusAndPutInFlux(hitDTOList, fluxSink);
        });
        hitDTOFlux.subscribe(p -> {
            filteredResult.add(p);
        });
        assert(filteredResult.size() < hitSize);
    }
}
