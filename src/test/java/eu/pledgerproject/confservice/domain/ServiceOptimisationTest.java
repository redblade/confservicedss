package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class ServiceOptimisationTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ServiceOptimisation.class);
        ServiceOptimisation serviceOptimisation1 = new ServiceOptimisation();
        serviceOptimisation1.setId(1L);
        ServiceOptimisation serviceOptimisation2 = new ServiceOptimisation();
        serviceOptimisation2.setId(serviceOptimisation1.getId());
        assertThat(serviceOptimisation1).isEqualTo(serviceOptimisation2);
        serviceOptimisation2.setId(2L);
        assertThat(serviceOptimisation1).isNotEqualTo(serviceOptimisation2);
        serviceOptimisation1.setId(null);
        assertThat(serviceOptimisation1).isNotEqualTo(serviceOptimisation2);
    }
}
