package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class ServiceReportTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ServiceReport.class);
        ServiceReport serviceReport1 = new ServiceReport();
        serviceReport1.setId(1L);
        ServiceReport serviceReport2 = new ServiceReport();
        serviceReport2.setId(serviceReport1.getId());
        assertThat(serviceReport1).isEqualTo(serviceReport2);
        serviceReport2.setId(2L);
        assertThat(serviceReport1).isNotEqualTo(serviceReport2);
        serviceReport1.setId(null);
        assertThat(serviceReport1).isNotEqualTo(serviceReport2);
    }
}
