package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class SteadyServiceTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SteadyService.class);
        SteadyService steadyService1 = new SteadyService();
        steadyService1.setId(1L);
        SteadyService steadyService2 = new SteadyService();
        steadyService2.setId(steadyService1.getId());
        assertThat(steadyService1).isEqualTo(steadyService2);
        steadyService2.setId(2L);
        assertThat(steadyService1).isNotEqualTo(steadyService2);
        steadyService1.setId(null);
        assertThat(steadyService1).isNotEqualTo(steadyService2);
    }
}
