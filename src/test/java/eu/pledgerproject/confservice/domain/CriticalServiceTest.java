package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class CriticalServiceTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CriticalService.class);
        CriticalService criticalService1 = new CriticalService();
        criticalService1.setId(1L);
        CriticalService criticalService2 = new CriticalService();
        criticalService2.setId(criticalService1.getId());
        assertThat(criticalService1).isEqualTo(criticalService2);
        criticalService2.setId(2L);
        assertThat(criticalService1).isNotEqualTo(criticalService2);
        criticalService1.setId(null);
        assertThat(criticalService1).isNotEqualTo(criticalService2);
    }
}
