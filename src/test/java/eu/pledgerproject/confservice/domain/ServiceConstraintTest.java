package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class ServiceConstraintTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ServiceConstraint.class);
        ServiceConstraint serviceConstraint1 = new ServiceConstraint();
        serviceConstraint1.setId(1L);
        ServiceConstraint serviceConstraint2 = new ServiceConstraint();
        serviceConstraint2.setId(serviceConstraint1.getId());
        assertThat(serviceConstraint1).isEqualTo(serviceConstraint2);
        serviceConstraint2.setId(2L);
        assertThat(serviceConstraint1).isNotEqualTo(serviceConstraint2);
        serviceConstraint1.setId(null);
        assertThat(serviceConstraint1).isNotEqualTo(serviceConstraint2);
    }
}
