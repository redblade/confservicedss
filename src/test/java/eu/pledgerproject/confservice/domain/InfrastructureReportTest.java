package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class InfrastructureReportTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(InfrastructureReport.class);
        InfrastructureReport infrastructureReport1 = new InfrastructureReport();
        infrastructureReport1.setId(1L);
        InfrastructureReport infrastructureReport2 = new InfrastructureReport();
        infrastructureReport2.setId(infrastructureReport1.getId());
        assertThat(infrastructureReport1).isEqualTo(infrastructureReport2);
        infrastructureReport2.setId(2L);
        assertThat(infrastructureReport1).isNotEqualTo(infrastructureReport2);
        infrastructureReport1.setId(null);
        assertThat(infrastructureReport1).isNotEqualTo(infrastructureReport2);
    }
}
