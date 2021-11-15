package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class BenchmarkReportTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BenchmarkReport.class);
        BenchmarkReport benchmarkReport1 = new BenchmarkReport();
        benchmarkReport1.setId(1L);
        BenchmarkReport benchmarkReport2 = new BenchmarkReport();
        benchmarkReport2.setId(benchmarkReport1.getId());
        assertThat(benchmarkReport1).isEqualTo(benchmarkReport2);
        benchmarkReport2.setId(2L);
        assertThat(benchmarkReport1).isNotEqualTo(benchmarkReport2);
        benchmarkReport1.setId(null);
        assertThat(benchmarkReport1).isNotEqualTo(benchmarkReport2);
    }
}
