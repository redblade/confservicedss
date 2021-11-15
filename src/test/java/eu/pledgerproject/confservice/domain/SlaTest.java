package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class SlaTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Sla.class);
        Sla sla1 = new Sla();
        sla1.setId(1L);
        Sla sla2 = new Sla();
        sla2.setId(sla1.getId());
        assertThat(sla1).isEqualTo(sla2);
        sla2.setId(2L);
        assertThat(sla1).isNotEqualTo(sla2);
        sla1.setId(null);
        assertThat(sla1).isNotEqualTo(sla2);
    }
}
