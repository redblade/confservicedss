package eu.pledgerproject.confservice.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import eu.pledgerproject.confservice.web.rest.TestUtil;

public class InfrastructureTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Infrastructure.class);
        Infrastructure infrastructure1 = new Infrastructure();
        infrastructure1.setId(1L);
        Infrastructure infrastructure2 = new Infrastructure();
        infrastructure2.setId(infrastructure1.getId());
        assertThat(infrastructure1).isEqualTo(infrastructure2);
        infrastructure2.setId(2L);
        assertThat(infrastructure1).isNotEqualTo(infrastructure2);
        infrastructure1.setId(null);
        assertThat(infrastructure1).isNotEqualTo(infrastructure2);
    }
}
