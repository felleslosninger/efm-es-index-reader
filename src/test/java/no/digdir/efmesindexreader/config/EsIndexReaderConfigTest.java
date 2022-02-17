package no.digdir.efmesindexreader.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EsIndexReaderConfigTest {

    @InjectMocks
    EsIndexReaderConfig config;

    @Before
    public void setup() {

    }

    @Test
    public void getIndexNameAndItShouldMatchExpectedPeriod() {
        String indexName = config.getIndexName();
        assert indexName.contains(".");
    }

    @Test
    public void getIndexNameAndSubStringShouldNotContainDashes() {
        String substring = config.getIndexName().substring(7, 17);
        assert !substring.contains("-");
    }
}
