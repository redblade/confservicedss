package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class PenaltyTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Penalty.class);
        Penalty penalty1 = new Penalty();
        penalty1.setId(1L);
        Penalty penalty2 = new Penalty();
        penalty2.setId(penalty1.getId());
        assertThat(penalty1).isEqualTo(penalty2);
        penalty2.setId(2L);
        assertThat(penalty1).isNotEqualTo(penalty2);
        penalty1.setId(null);
        assertThat(penalty1).isNotEqualTo(penalty2);
    }
}
