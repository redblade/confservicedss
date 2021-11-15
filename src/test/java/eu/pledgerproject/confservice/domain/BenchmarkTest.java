package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class BenchmarkTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Benchmark.class);
        Benchmark benchmark1 = new Benchmark();
        benchmark1.setId(1L);
        Benchmark benchmark2 = new Benchmark();
        benchmark2.setId(benchmark1.getId());
        assertThat(benchmark1).isEqualTo(benchmark2);
        benchmark2.setId(2L);
        assertThat(benchmark1).isNotEqualTo(benchmark2);
        benchmark1.setId(null);
        assertThat(benchmark1).isNotEqualTo(benchmark2);
    }
}
