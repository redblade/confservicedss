package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class SlaViolationTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SlaViolation.class);
        SlaViolation slaViolation1 = new SlaViolation();
        slaViolation1.setId(1L);
        SlaViolation slaViolation2 = new SlaViolation();
        slaViolation2.setId(slaViolation1.getId());
        assertThat(slaViolation1).isEqualTo(slaViolation2);
        slaViolation2.setId(2L);
        assertThat(slaViolation1).isNotEqualTo(slaViolation2);
        slaViolation1.setId(null);
        assertThat(slaViolation1).isNotEqualTo(slaViolation2);
    }
}
